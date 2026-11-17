package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;

import java.util.List;
import java.util.Optional;

/**
 * The inner world's own mob spawner. Vanilla spawning is driven by biome spawn lists, which for these void-based biomes
 * would spawn the wrong things at the wrong realms, so the inner world rolls from each region's own profile list instead.
 *
 * Every 3 seconds, for each player inside their own plot: if fewer than MAX_NEARBY of our mobs are within 64 blocks, pick
 * a spot 20-44 blocks away on dry, loaded, solid ground, look up the region's variant there, roll one mob from its
 * "mobs.spawns" by weight, and spawn a group of it. The realm range comes from realmRange(): the theme's own
 * min/max_realm without the Life Dao, multiplied up with it (see InnerWorldProfile.Mobs).
 *
 * Confidence note: EntityType.create(level, EntitySpawnReason) and Mob.finalizeSpawn(...) match the current signatures as
 * far as I know them, but I haven't seen your build's versions. The two calls are isolated in spawnOne().
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldMobs {
    private InnerWorldMobs() {
    }

    /**
     * The mobs this spawner created, by UUID. (Entity tags aren't available on Mob in this build, and a set is enough: it only
     * has to cap how many of OUR mobs are near a player. It is not saved, so after a restart old mobs simply stop counting
     * and vanilla despawning removes the ones that wander far away.)
     */
    private static final java.util.Set<java.util.UUID> SPAWNED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    static boolean isSpawned(Mob mob) {
        return SPAWNED.contains(mob.getUUID());
    }
    /** A spawn entry with no per-mob overrides, for rolling a region's base realm range (e.g. structure inhabitants). */
    static final InnerWorldProfile.MobSpawn NO_OVERRIDE =
            new InnerWorldProfile.MobSpawn(null, 1.0, 1, 1, Optional.empty(), Optional.empty());
    private static final int INTERVAL_TICKS = 60;
    private static final int MAX_NEARBY = 14;
    private static final int MIN_DISTANCE = 20;
    private static final int MAX_DISTANCE = 44;

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
            if (plot == null) {
                lastStatus = "no plot for " + player.getName().getString();
                continue;
            }
            if (plot.generatorVersion < 1) {
                lastStatus = "this is a LEGACY plot (generator version 0): the spawner only works on seeded plots. Use a fresh plot";
                continue;
            }
            if (plot.seed == null) {
                lastStatus = "plot has no seed yet (cast Inner World with a path first)";
                continue;
            }
            trySpawn(server, level, player, plot);
        }
    }

    /** Why the spawner last did or didn't spawn, shown by /ascension innerworld debug. */
    static volatile String lastStatus = "has not run yet (needs a seeded plot and a player inside the inner world)";

    static int countNear(ServerLevel level, ServerPlayer player) {
        return level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64.0), InnerWorldMobs::isSpawned).size();
    }

    private static void trySpawn(MinecraftServer server, ServerLevel level, ServerPlayer player, InnerWorldPlots.Plot plot) {
        if (level.getGameTime() % 600 < INTERVAL_TICKS) {
            SPAWNED.removeIf(id -> level.getEntity(id) == null); // forget mobs that are gone
        }
        int nearby = countNear(level, player);
        if (nearby >= MAX_NEARBY) {
            lastStatus = "idle: cap reached (" + nearby + " of " + MAX_NEARBY + " of its mobs within 64 blocks)";
            return;
        }

        SeededWorld world = InnerWorldGrowth.seededFor(server, plot);
        BlockPos center = InnerWorld.plotCenter(plot.index);
        RandomSource random = level.getRandom();
        int lifeRealm = InnerWorldLife.lifeRealm(player);
        String why = "no attempt made";

        for (int attempt = 0; attempt < 3; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            if (!level.isLoaded(new BlockPos(x, InnerWorld.SURFACE_Y, z))) {
                why = "chosen spot not loaded (" + x + ", " + z + ")";
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState below = level.getBlockState(pos.below());
            if (below.isAir() || !below.getFluidState().isEmpty() || !level.getBlockState(pos).isAir()) {
                why = "no dry solid ground at the chosen spot (" + x + ", " + y + ", " + z + "): below is " + below.getBlock();
                continue;
            }

            InnerWorldProfile.Variant variant = world.variantAt(x - center.getX(), z - center.getZ());
            InnerWorldProfile.Mobs mobs = variant.mobs();
            InnerWorldProfile.MobSpawn spawn = pick(mobs.spawns(), random);
            if (spawn == null) {
                why = "this region has NO mobs.spawns in its profile (biome " + variant.biome().map(Object::toString).orElse("none")
                        + "): the profile JSON is probably an older copy without the mobs block";
                continue;
            }
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(spawn.entity());
            if (type.isEmpty()) {
                why = "unknown entity id '" + spawn.entity() + "' in a profile";
                continue;
            }

            int[] range = realmRange(mobs, spawn, lifeRealm);
            int group = spawn.minGroup() + random.nextInt(Math.max(1, spawn.maxGroup() - spawn.minGroup() + 1));
            int spawned = 0;
            for (int i = 0; i < group && nearby + i < MAX_NEARBY; i++) {
                int sx = x + random.nextInt(5) - 2;
                int sz = z + random.nextInt(5) - 2;
                int sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
                if (spawnOne(level, type.get(), new BlockPos(sx, sy, sz), range, random)) {
                    spawned++;
                }
            }
            lastStatus = spawned > 0
                    ? "spawned " + spawned + " x " + spawn.entity() + " (realm " + range[0] + ".." + range[1] + ") near " + x + ", " + y + ", " + z
                    : "chose " + spawn.entity() + " but spawnOne refused it (create() returned null, not a Mob, or the join was cancelled)";
            return;
        }
        lastStatus = "3 attempts failed; last: " + why;
    }

    /**
     * Without the Life Dao: [min, max] straight from the theme (a per-mob override can only narrow it, never exceed the theme's
     * cap). With it: both ends are scaled by (1 + life_multiplier_per_realm x lifeMajorRealm) around realm 1, so low realms move
     * up and the top end rises with it: scale(v) = (v + 1) x multiplier - 1.
     */
    static int[] realmRange(InnerWorldProfile.Mobs mobs, InnerWorldProfile.MobSpawn spawn, int lifeRealm) {
        int min = spawn.minRealm().orElse(mobs.minRealm());
        int max = Math.min(spawn.maxRealm().orElse(mobs.maxRealm()), mobs.maxRealm());
        max = Math.max(min, max);
        if (lifeRealm >= InnerWorldLife.REQUIRED_MAJOR_REALM) {
            double multiplier = 1.0 + mobs.lifeMultiplierPerRealm() * lifeRealm;
            min = scale(min, multiplier);
            max = Math.max(min, scale(max, multiplier));
        }
        return new int[]{min, max};
    }

    private static int scale(int value, double multiplier) {
        return Math.max(0, (int) Math.round((value + 1) * multiplier) - 1);
    }

    private static InnerWorldProfile.MobSpawn pick(List<InnerWorldProfile.MobSpawn> spawns, RandomSource random) {
        double total = 0.0;
        for (InnerWorldProfile.MobSpawn spawn : spawns) {
            total += Math.max(0.0, spawn.weight());
        }
        if (total <= 0.0) {
            return null;
        }
        double roll = random.nextDouble() * total;
        for (InnerWorldProfile.MobSpawn spawn : spawns) {
            roll -= Math.max(0.0, spawn.weight());
            if (roll <= 0.0) {
                return spawn;
            }
        }
        return spawns.get(spawns.size() - 1);
    }

    private static boolean spawnOne(ServerLevel level, EntityType<?> type, BlockPos pos, int[] range, RandomSource random) {
        Entity entity = type.create(level, EntitySpawnReason.NATURAL);
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        SPAWNED.add(mob.getUUID());
        // Through EventHooks so NeoForge fires FinalizeSpawnEvent (your MobEventHandler rolls a tier there)...
        EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
        // ...then the inner world replaces that roll with one inside the allowed realm range, before the mob joins the level.
        InnerWorldMobRealms.assign(mob, range[0], range[1], random);
        return level.addFreshEntity(mob);
    }
}