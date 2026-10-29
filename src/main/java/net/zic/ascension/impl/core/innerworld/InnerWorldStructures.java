package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobSpawnHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vanilla and modded structures in the inner world, picked per region by each profile's "structures" list.
 *
 * WHY THIS IS NOT THE DIMENSION'S CHUNK GENERATOR: terrain here is written by InnerWorldGrowth after a chunk already
 * exists, but vanilla plans structures while a chunk is first created, i.e. over a flat void, before any terrain exists. So
 * structures are planned and placed here instead, calling the same vanilla code (Structure.generate, then
 * StructureStart.placeInChunk) through InnerWorldHeightGenerator, which answers every height question with our real terrain.
 * Villages therefore use the real jigsaw generation: same pools, same roads and houses, same villagers.
 *
 * RULES
 *  - Sites sit on a deterministic 160-block grid per plot; a profile entry's "spacing" sets how often it appears.
 *  - Entries with requires_life (default) only generate once the owner's life path is at major realm 1+; ruins and
 *    similar (requires_life false) generate for everyone. Cells that roll "nothing" are final; cells with only
 *    life-gated entries stay open until the owner earns the Life Dao.
 *  - A structure is only placed when EVERY chunk it covers is already built terrain, so a ring can never grow into a
 *    half-built village. Sites near the frontier wait until later rings arrive.
 *  - Sites over water, banks, mesas or the dais are skipped. Rigid jigsaw pieces (houses) get a levelled pad, a thin
 *    version of vanilla's terrain beard; roads follow the ground as in vanilla.
 *  - Inhabitants (villagers etc.) get a mob cultivation realm from the region's mob range, like spawned mobs.
 * At most one structure is placed per plot every 5 seconds.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldStructures {
    private InnerWorldStructures() {
    }

    /**
     * CRASH SWITCH (see InnerWorldQi.ADD_PATH_AFFINITY). Giving already-placed villagers and cats a mob cultivation instance adds and
     * removes trait path bonuses on entities that have already joined the level, a path your normal flow never takes (it applies at
     * join). If the cat crash persists with the other switch off, this is the next suspect. Off until confirmed.
     */
    private static final boolean GIVE_INHABITANTS_REALMS = false;

    private static final int CELL = 160;
    private static final int INTERVAL_TICKS = 100;
    private static final java.util.Set<java.util.UUID> POPULATED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Why structures last did or didn't happen, shown by /ascension innerworld debug. */
    static volatile String lastStatus = "has not run yet (needs a seeded plot, a player inside, and a built radius of 6+ chunks)";
    static volatile int placedCount;

    private static final Map<String, Integer> RETRY = new HashMap<>();
    private static InnerWorldHeightGenerator generator;
    private static MinecraftServer generatorServer;
    private static final java.util.Set<String> WARNED = new java.util.HashSet<>();

    // ------------------------------------------------------------------------------------------------ driving

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel level = server.getLevel(InnerWorld.DIMENSION);
        if (level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
            if (plot != null && plot.generatorVersion >= 1 && plot.seed != null) {
                scan(server, level, player, plot);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        generator = null;
        generatorServer = null;
        RETRY.clear();
        WARNED.clear();
    }

    /** Terrain height for InnerWorldHeightGenerator, or null outside every plot. */
    static Integer surface(MinecraftServer server, int x, int z, boolean includeWater) {
        InnerWorldPlots.Plot plot = InnerWorldQi.plotForChunk(x >> 4, z >> 4);
        if (plot == null || plot.generatorVersion < 1 || plot.seed == null) {
            return null;
        }
        BlockPos center = InnerWorld.plotCenter(plot.index);
        return InnerWorldGrowth.seededFor(server, plot).surfaceY(x - center.getX(), z - center.getZ(), includeWater);
    }

    static InnerWorldHeightGenerator generator(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (generator == null || generatorServer != server) {
            // the dimension is a flat generator (inner_world.json); we only borrow its settings
            FlatLevelSource flat = (FlatLevelSource) level.getChunkSource().getGenerator();
            generator = new InnerWorldHeightGenerator(flat.settings(), server);
            generatorServer = server;
        }
        return generator;
    }

    // ------------------------------------------------------------------------------------------------ scanning

    private static final int MAX_ATTEMPTS = 6;
    private static final Map<String, Integer> ATTEMPTS = new HashMap<>();

    private static void scan(MinecraftServer server, ServerLevel level, ServerPlayer owner, InnerWorldPlots.Plot plot) {
        if (plot.generatedRadius < 6) {
            lastStatus = "world too small: built radius is " + plot.generatedRadius + " chunks, structures need 6+ and room for the whole footprint "
                    + "(a village needs roughly 12-16 chunks built around its site). Raise the tier: /ascension innerworld settier <player> <tier>";
            return;
        }
        int life = InnerWorldLife.lifeRealm(owner);
        if (life != plot.lifeRealm) {
            plot.lifeRealm = life;
            InnerWorldPlots.save();
        }
        boolean hasLife = life >= InnerWorldLife.REQUIRED_MAJOR_REALM;

        SeededWorld world = InnerWorldGrowth.seededFor(server, plot);
        BlockPos center = InnerWorld.plotCenter(plot.index);
        int built = plot.generatedRadius;
        int reach = built * 16 - 48; // keep sites well inside built land
        int min = Math.floorDiv(-reach, CELL);
        int max = Math.floorDiv(reach, CELL);

        // nearest cells first, so the structures around the spawn appear before the far ones
        List<int[]> cells = new ArrayList<>();
        for (int cx = min; cx <= max; cx++) {
            for (int cz = min; cz <= max; cz++) {
                cells.add(new int[]{cx, cz});
            }
        }
        cells.sort(java.util.Comparator.comparingDouble(c -> Math.hypot((c[0] + 0.5) * CELL, (c[1] + 0.5) * CELL)));

        int handled = 0;
        int waiting = 0;
        int open = 0;
        for (int[] cell : cells) {
            String key = cell[0] + ":" + cell[1];
            if (plot.structureSites.contains(key)) {
                handled++;
                continue;
            }
            Integer needed = RETRY.get(plot.index + "#" + key);
            if (needed != null && built < needed) {
                waiting++;
                continue; // its footprint needs more built land than exists yet
            }
            if (processCell(server, level, plot, world, center, cell[0], cell[1], key, hasLife, life)) {
                return; // one structure plan per scan
            }
            open++;
        }
        lastStatus = "scanned " + cells.size() + " grid cells at built radius " + built + " chunks: "
                + handled + " already decided, " + waiting + " waiting for more built land around them, " + open
                + " open (nothing eligible yet, e.g. only Life Dao structures and Life Dao is " + (hasLife ? "present" : "missing") + "). Placed so far: " + placedCount;
    }

    private static void done(InnerWorldPlots.Plot plot, String key) {
        plot.structureSites.add(key);
        InnerWorldPlots.save();
    }

    private static int[] sitePosition(long seed, int cx, int cz, int attempt) {
        int lx = (int) ((cx + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 9100 + attempt * 7L, cx, cz)) * CELL);
        int lz = (int) ((cz + 0.15 + 0.7 * InnerWorldNoise.hash(seed + 9101 + attempt * 7L, cx, cz)) * CELL);
        return new int[]{lx, lz};
    }

    /** Cheap check before the expensive planning: the spot and a ring 28 blocks around it must be dry, unraised and not too uneven. */
    private static boolean roughlyDry(SeededWorld world, int lx, int lz) {
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        for (int i = 0; i < 9; i++) {
            double angle = i * Math.PI / 4.0;
            int x = i == 8 ? lx : lx + (int) Math.round(Math.cos(angle) * 28.0);
            int z = i == 8 ? lz : lz + (int) Math.round(Math.sin(angle) * 28.0);
            SeededWorld.ColumnInfo info = world.columnInfo(x, z);
            if (info.wet() || info.raised() || info.dais()) {
                return false;
            }
            low = Math.min(low, info.top());
            high = Math.max(high, info.top());
        }
        return high - low <= 14;
    }

    /**
     * Returns true if it did heavy work (planned a structure), so the caller stops for this scan. A cell no longer dies on its
     * first bad spot: the entry is rolled once for the cell, then up to six positions inside it are tried (cheap dryness check
     * first, vanilla planning only for spots that pass), and the first one that really fits is used.
     */
    private static boolean processCell(MinecraftServer server, ServerLevel level, InnerWorldPlots.Plot plot, SeededWorld world,
                                       BlockPos center, int cx, int cz, String key, boolean hasLife, int life) {
        long seed = plot.seed.value;
        int[] base = sitePosition(seed, cx, cz, 0);
        if (Math.hypot(base[0], base[1]) < 70.0) {
            done(plot, key);
            return false;
        }

        InnerWorldProfile.Variant baseVariant = world.variantAt(base[0], base[1]);
        List<InnerWorldProfile.StructureEntry> eligible = new ArrayList<>();
        for (InnerWorldProfile.StructureEntry entry : baseVariant.structures()) {
            if (!entry.requiresLife() || hasLife) {
                eligible.add(entry);
            }
        }
        if (eligible.isEmpty()) {
            if (baseVariant.structures().isEmpty()) {
                done(plot, key); // nothing ever goes here
            }
            return false; // only life-gated entries: stay open until the Life Dao is earned
        }

        InnerWorldProfile.StructureEntry entry = pick(eligible, InnerWorldNoise.hash(seed + 9102, cx, cz));
        double chance = Math.min(1.0, Math.pow(CELL / (double) Math.max(1, entry.spacing()), 2.0));
        if (InnerWorldNoise.hash(seed + 9103, cx, cz) >= chance) {
            done(plot, key);
            return false;
        }

        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Optional<? extends Holder<Structure>> holder = registry.get(ResourceKey.create(Registries.STRUCTURE, entry.structure()));
        if (holder.isEmpty()) {
            lastStatus = "cell " + key + ": unknown structure id '" + entry.structure() + "' (renamed in this version, or the mod isn't installed)";
            if (WARNED.add(entry.structure().toString())) {
                AscensionCraft.LOGGER.warn("Inner World structures: unknown structure '{}' in a profile, skipping.", entry.structure());
            }
            done(plot, key);
            return false;
        }

        InnerWorldHeightGenerator gen = generator(level);
        int ccx = center.getX() >> 4;
        int ccz = center.getZ() >> 4;
        int target = InnerWorld.radiusChunksForTier(plot.tier);
        String attemptKey = plot.index + "#" + key;
        int attempt = ATTEMPTS.getOrDefault(attemptKey, 0);
        int planned = 0;
        String lastWhy = "no position tried";

        for (; attempt < MAX_ATTEMPTS && planned < 2; attempt++) {
            int[] pos = sitePosition(seed, cx, cz, attempt);
            if (Math.hypot(pos[0], pos[1]) < 70.0) {
                lastWhy = "position too close to the spawn dais";
                continue;
            }
            if (!roughlyDry(world, pos[0], pos[1])) {
                lastWhy = "position " + pos[0] + "," + pos[1] + " is wet, raised or too uneven";
                continue;
            }

            planned++;
            BlockPos site = new BlockPos(center.getX() + pos[0], 0, center.getZ() + pos[1]);
            long structureSeed = seed ^ (cx * 341873128712L) ^ (cz * 132897987541L) ^ (attempt * 7919L);
            StructureStart start = holder.get().value().generate(holder.get(), level.dimension(), level.registryAccess(), gen,
                    gen.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(), structureSeed,
                    ChunkPos.containing(site), 0, level, biome -> true);
            if (!start.isValid()) {
                lastWhy = entry.structure() + " found no valid generation point at that position";
                continue;
            }

            BoundingBox box = start.getBoundingBox();
            int needed = 0;
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
                    needed = Math.max(needed, Math.max(Math.abs(x - ccx), Math.abs(z - ccz)));
                }
            }
            if (needed > target) {
                lastWhy = entry.structure() + " needs " + needed + " built chunks around the plot centre but the world only grows to " + target + " at this tier";
                continue;
            }
            if (needed > plot.generatedRadius) {
                // plan is fine, the land just isn't built yet: wait until exactly that radius exists (no pointless retries per ring)
                RETRY.put(attemptKey, needed);
                ATTEMPTS.put(attemptKey, attempt);
                lastStatus = "cell " + key + ": " + entry.structure() + " planned, needs " + needed + " built chunks, only " + plot.generatedRadius
                        + " built so far; waiting";
                return true;
            }
            if (!suitable(world, center, start)) {
                lastWhy = entry.structure() + " would sit on water, raised ground or steep terrain at that position";
                continue;
            }

            // Trees already standing in the footprint would carry the roads on their canopy (road pieces follow the highest
            // surface, leaves included), so every tree part in and just around the footprint is cleared first, and the footprint is
            // remembered so no tree is planted there later.
            clearTrees(level, world, center, box);
            plot.structureBoxes.add(new int[]{box.minX() - 2, box.minZ() - 2, box.maxX() + 2, box.maxZ() + 2});
            InnerWorldPlots.save();
            flatten(level, world, center, start);
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
                    BoundingBox writable = new BoundingBox(x * 16, level.getMinY() + 1, z * 16, x * 16 + 15, level.getMaxY(), z * 16 + 15);
                    RandomSource random = RandomSource.create(structureSeed ^ ChunkPos.pack(x, z));
                    start.placeInChunk(level, level.structureManager(), gen, random, writable, new ChunkPos(x, z));
                }
            }
            done(plot, key);
            ATTEMPTS.remove(attemptKey);
            placedCount++;
            lastStatus = "placed " + entry.structure() + " at " + box.getCenter() + " (position attempt " + (attempt + 1) + ")";
            AscensionCraft.LOGGER.info("Inner World: placed {} at {} (plot {}).", entry.structure(), box.getCenter(), plot.index);
            if (GIVE_INHABITANTS_REALMS) {
                populate(level, box, world.variantAt(pos[0], pos[1]), life, RandomSource.create(structureSeed));
            }
            return true;
        }

        if (attempt >= MAX_ATTEMPTS) {
            lastStatus = "cell " + key + ": gave up after " + MAX_ATTEMPTS + " positions; last reason: " + lastWhy;
            done(plot, key);
            ATTEMPTS.remove(attemptKey);
        } else {
            ATTEMPTS.put(attemptKey, attempt);
            lastStatus = "cell " + key + ": still searching (attempt " + attempt + "/" + MAX_ATTEMPTS + "); last reason: " + lastWhy;
        }
        return planned > 0;
    }

    private static InnerWorldProfile.StructureEntry pick(List<InnerWorldProfile.StructureEntry> entries, double roll01) {
        double total = 0.0;
        for (InnerWorldProfile.StructureEntry entry : entries) {
            total += Math.max(0.0, entry.weight());
        }
        double roll = roll01 * total;
        for (InnerWorldProfile.StructureEntry entry : entries) {
            roll -= Math.max(0.0, entry.weight());
            if (roll <= 0.0) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    /**
     * Judged where it matters. For jigsaw structures (villages and the like) every RIGID piece, i.e. every house, must stand on
     * dry, unraised ground that is reasonably level under it; roads and other terrain-matching pieces may cross anything, as in
     * vanilla. At most one rigid piece in ten may fail. For other structures the whole footprint must be mostly dry (under 15%
     * wet), unraised, off the dais and not too uneven.
     */
    private static boolean suitable(SeededWorld world, BlockPos center, StructureStart start) {
        int rigid = 0;
        int bad = 0;
        for (StructurePiece piece : start.getPieces()) {
            if (!(piece instanceof PoolElementStructurePiece pool)
                    || pool.getElement().getProjection() != StructureTemplatePool.Projection.RIGID) {
                continue;
            }
            rigid++;
            BoundingBox pb = pool.getBoundingBox();
            int low = Integer.MAX_VALUE;
            int high = Integer.MIN_VALUE;
            boolean spoiled = false;
            for (int x = pb.minX(); x <= pb.maxX() && !spoiled; x += 3) {
                for (int z = pb.minZ(); z <= pb.maxZ(); z += 3) {
                    SeededWorld.ColumnInfo info = world.columnInfo(x - center.getX(), z - center.getZ());
                    if (info.wet() || info.raised() || info.dais()) {
                        spoiled = true;
                        break;
                    }
                    low = Math.min(low, info.top());
                    high = Math.max(high, info.top());
                }
            }
            if (spoiled || high - low > 8) {
                bad++;
            }
        }
        if (rigid > 0) {
            return bad <= rigid / 10;
        }

        BoundingBox box = start.getBoundingBox();
        int samples = 0;
        int wet = 0;
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        for (int x = box.minX(); x <= box.maxX(); x += 4) {
            for (int z = box.minZ(); z <= box.maxZ(); z += 4) {
                SeededWorld.ColumnInfo info = world.columnInfo(x - center.getX(), z - center.getZ());
                if (info.raised() || info.dais()) {
                    return false;
                }
                samples++;
                if (info.wet()) {
                    wet++;
                }
                low = Math.min(low, info.top());
                high = Math.max(high, info.top());
            }
        }
        return wet <= samples * 0.15 && high - low <= 16;
    }

    /**
     * Removes trees (logs, leaves, giant mushrooms, vines) from the structure footprint plus a 3-block margin. The cheap test is
     * the motion-blocking heightmap: a column whose highest solid block is just the ground has nothing tall to clear.
     */
    private static void clearTrees(ServerLevel level, SeededWorld world, BlockPos center, BoundingBox box) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = box.minX() - 3; x <= box.maxX() + 3; x++) {
            for (int z = box.minZ() - 3; z <= box.maxZ() + 3; z++) {
                SeededWorld.ColumnInfo info = world.columnInfo(x - center.getX(), z - center.getZ());
                if (info.wet() || info.dais() || info.raised()) {
                    continue;
                }
                int ground = info.top();
                if (level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z) <= ground + 1) {
                    continue; // nothing taller than the ground here
                }
                for (int y = ground + 1; y <= ground + 48; y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.LOGS)
                            || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                            || state.is(Blocks.MUSHROOM_STEM) || state.is(Blocks.VINE)) {
                        level.setBlock(pos, air, 2);
                    }
                }
            }
        }
    }

    /**
     * A thin stand-in for vanilla's terrain beard: under every RIGID jigsaw piece (houses and the like) the ground is raised
     * or cut to the piece's own ground level, with a one-block margin, and anything growing above it (trees, plants) is
     * cleared. Terrain-matching pieces (roads) are left alone and follow the surface exactly like in vanilla.
     */
    private static void flatten(ServerLevel level, SeededWorld world, BlockPos center, StructureStart start) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (StructurePiece piece : start.getPieces()) {
            if (!(piece instanceof PoolElementStructurePiece pool)
                    || pool.getElement().getProjection() != StructureTemplatePool.Projection.RIGID) {
                continue;
            }
            BoundingBox pb = pool.getBoundingBox();
            int ground = pb.minY() + pool.getGroundLevelDelta() - 1;
            for (int x = pb.minX() - 1; x <= pb.maxX() + 1; x++) {
                for (int z = pb.minZ() - 1; z <= pb.maxZ() + 1; z++) {
                    int lx = x - center.getX();
                    int lz = z - center.getZ();
                    SeededWorld.ColumnInfo info = world.columnInfo(lx, lz);
                    if (info.wet() || info.dais()) {
                        continue;
                    }
                    InnerWorldProfile.Palette palette = world.variantAt(lx, lz).palette();
                    int top = info.top();
                    BlockState subState = palette.sub().defaultBlockState();
                    BlockState topState = palette.top().defaultBlockState();
                    if (top < ground) {
                        for (int y = top + 1; y < ground; y++) {
                            level.setBlock(pos.set(x, y, z), subState, 2);
                        }
                        level.setBlock(pos.set(x, ground, z), topState, 2);
                    } else if (top > ground) {
                        for (int y = ground + 1; y <= top; y++) {
                            level.setBlock(pos.set(x, y, z), air, 2);
                        }
                        level.setBlock(pos.set(x, ground, z), topState, 2);
                    }
                    for (int y = ground + 1; y <= pb.maxY() + 3; y++) {
                        if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                            level.setBlock(pos, air, 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Villagers and other structure inhabitants are placed by the vanilla templates without going through spawning, so they
     * have no mob cultivation. Give them the region's realm range (raised by the Life Dao) exactly like spawned mobs.
     */
    private static void populate(ServerLevel level, BoundingBox box, InnerWorldProfile.Variant variant, int life, RandomSource random) {
        AABB area = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
        int[] range = InnerWorldMobs.realmRange(variant.mobs(), InnerWorldMobs.NO_OVERRIDE, life);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            if (InnerWorldMobs.isSpawned(mob) || !POPULATED.add(mob.getUUID()) || !MobSpawnHelper.hasConfig(mob)) {
                continue;
            }
            InnerWorldMobRealms.assign(mob, range[0], range[1], random);
            InnerWorldMobRealms.applyNow(mob);
        }
    }
}