package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.herbs.ModHerbs;

/**
 * Wild herbs in inner worlds, using your own HerbDefinition rules for everything it already decides:
 * canWildSurviveOn(support), canSpawn(level, pos, random) (this includes the qi checks, which read the chunk's biome
 * config, so this runs AFTER the theme biomes are applied), chooseWildAgeTier and chooseWildQualityTier.
 *
 * On top of that the world adds: +1 age tier per 3 world tiers; +1 quality tier per 2 world tiers; up to +1.5 tiers
 * from the herb patch's luck (24-block patches share a roll); and the profile's own quality_bonus. Quality is capped by
 * the herb's qualityCap. Herb sites are a pure function of the seed, so a harvested herb REGROWS at the same spot:
 * every 5 minutes, near each player, empty herb sites are replanted as fresh stage-0 seedlings and your normal herb
 * growth takes over from there (age and quality then grow the way they do everywhere else).
 *
 * The one unwired step is InnerWorldHerbPlacer.place: I don't have your herb block / block entity classes.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldHerbs {
    private InnerWorldHerbs() {
    }

    /**
     * Diagnostics: why herb sites did or didn't become herbs. A summary is logged every 25 populated chunks, e.g.
     * "[InnerWorldHerbs] sites=40 unknownHerb=0 occupied=0 support=40 ..." means every site was rejected by the support block
     * (the herb's naturalSupport doesn't match the surface block of that variant). Remove once herbs spawn.
     */
    private static final int[] STATS = new int[8];
    private static int chunksSinceLog;

    private static final int REGROW_INTERVAL_TICKS = 6000;
    private static final int REGROW_RADIUS = 48;

    /** Called once a seeded chunk has its terrain and biomes. */
    static void populateChunk(ServerLevel level, SeededWorld world, InnerWorldPlots.Plot plot,
                              int chunkX, int chunkZ, int centerX, int centerZ) {
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkX * 16 + dx;
                int z = chunkZ * 16 + dz;
                tryPlace(level, world, plot, x, z, x - centerX, z - centerZ, false);
            }
        }
        if (++chunksSinceLog >= 25) {
            chunksSinceLog = 0;
            AscensionCraft.LOGGER.info("[InnerWorldHerbs] sites={} unknownHerb={} occupied={} support={} spawnRule={} qi={} placeFailed={} placed={}",
                    STATS[0], STATS[1], STATS[2], STATS[3], STATS[4], STATS[5], STATS[6], STATS[7]);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (event.getServer().getTickCount() % REGROW_INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(InnerWorld.DIMENSION);
        if (level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
            if (plot == null || plot.generatorVersion < 1 || plot.seed == null) {
                continue;
            }
            SeededWorld world = InnerWorldGrowth.seededFor(event.getServer(), plot);
            BlockPos center = InnerWorld.plotCenter(plot.index);
            int px = player.blockPosition().getX();
            int pz = player.blockPosition().getZ();
            for (int x = px - REGROW_RADIUS; x <= px + REGROW_RADIUS; x++) {
                for (int z = pz - REGROW_RADIUS; z <= pz + REGROW_RADIUS; z++) {
                    if (level.isLoaded(new BlockPos(x, InnerWorld.SURFACE_Y, z))) {
                        tryPlace(level, world, plot, x, z, x - center.getX(), z - center.getZ(), true);
                    }
                }
            }
        }
    }

    /**
     * HerbDefinition.canSpawn reads the qi of the biome's BiomeConfiguration data map, which inner world biomes (plain
     * vanilla biomes) don't have, so any herb with a qi requirement could never spawn. The inner world has its own
     * ambient qi instead (InnerWorldQi), so check the herb's requirements against THAT: the region's capacity scaled by
     * the world's tier, and the region's path affinity (matched by full id or by its last segment, like your chunk code).
     */
    private static boolean meetsInnerWorldQi(HerbDefinition herb, SeededWorld.HerbSite site, InnerWorldPlots.Plot plot) {
        double tierScale = 1.0 + plot.tier * 0.5;
        InnerWorldProfile.Qi qi = site.variant().qi();
        if (!herb.qiProfile().capacity().meetsMinimum(qi.capacity() * tierScale)) {
            return false;
        }
        for (java.util.Map.Entry<net.minecraft.resources.Identifier, HerbDefinition.QiRequirement> entry : herb.qiProfile().affinities().entrySet()) {
            double have = 0.0;
            net.minecraft.resources.Identifier path = site.path();
            if (path != null && (path.equals(entry.getKey()) || lastSegment(path).equals(lastSegment(entry.getKey())))) {
                have = qi.affinity() * tierScale;
            }
            if (!entry.getValue().meetsMinimum(have)) {
                return false;
            }
        }
        return true;
    }

    private static String lastSegment(net.minecraft.resources.Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static void tryPlace(ServerLevel level, SeededWorld world, InnerWorldPlots.Plot plot,
                                 int x, int z, int lx, int lz, boolean regrow) {
        SeededWorld.HerbSite site = world.herbAt(lx, lz);
        if (site == null) {
            return;
        }
        STATS[0]++;
        HerbDefinition herb = ModHerbs.get(site.spawn().herb());
        if (herb == null) {
            STATS[1]++;
            return;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState here = level.getBlockState(pos);
        if (!(here.isAir() || here.canBeReplaced())) {
            STATS[2]++;
            return; // something (a harvested stump, a boulder, a player build) already occupies the site
        }
        if (!herb.canWildSurviveOn(level.getBlockState(pos.below()))) {
            STATS[3]++;
            return;
        }

        RandomSource random = RandomSource.create(world.herbRollSeed(lx, lz));
        if (!herb.canSpawnIgnoringQi(level, pos, random)) {
            STATS[4]++;
            return;
        }
        if (!meetsInnerWorldQi(herb, site, plot)) {
            STATS[5]++;
            return;
        }

        int ageTier;
        int qualityTier;
        int stage;
        if (regrow) {
            ageTier = 0;
            qualityTier = 0;
            stage = 0;
        } else {
            ageTier = Math.min(herb.maxAgeTier(), herb.chooseWildAgeTier(random) + plot.tier / 3);
            int luck = (int) Math.floor(site.patchLuck() * 1.5);
            qualityTier = herb.chooseWildQualityTier(random) + luck + plot.tier / 2 + site.spawn().qualityBonus();
            qualityTier = Math.max(0, Math.min(qualityTier, herb.qualityCap().ordinal()));
            stage = herb.maxGrowthStage();
        }
        if (InnerWorldHerbPlacer.place(level, pos, herb, ageTier, qualityTier, stage)) {
            STATS[7]++;
        } else {
            STATS[6]++;
        }
    }
}