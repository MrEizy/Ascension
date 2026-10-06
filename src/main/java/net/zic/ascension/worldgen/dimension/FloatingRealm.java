package net.zic.ascension.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.zic.ascension.AscensionCraft;

/**
 * The floating realm: outer-end style islands made of stone, dirt and grass under an overworld sky.
 * Terrain is defined in data/ascension/worldgen/noise_settings/floating_realm.json.
 */
public final class FloatingRealm {
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, AscensionCraft.prefix("floating_realm"));

    /** Spacing between sampled columns while searching for an island to land on. */
    private static final int SEARCH_STEP = 16;
    /** Furthest distance from 0,0 searched for an island before falling back to a platform. */
    private static final int SEARCH_RADIUS = 1024;
    private static final int FALLBACK_Y = 80;

    //the landing spot only depends on the seed, so it is found once per level
    private static ServerLevel cachedLevel;
    private static BlockPos cachedLanding;

    private FloatingRealm() {
    }

    /**
     * Finds the closest island surface to the realm's centre, searching outward in rings.
     * Heights come from the generator's noise, so no chunks are generated while searching.
     * If nothing is found a small grass platform is placed at the centre.
     */
    public static BlockPos findLanding(ServerLevel level) {
        if (level == cachedLevel && cachedLanding != null) {
            return cachedLanding;
        }

        BlockPos landing = searchIsland(level);
        if (landing == null) {
            landing = new BlockPos(0, FALLBACK_Y + 1, 0);
            placePlatform(level, landing.below());
        }

        cachedLevel = level;
        cachedLanding = landing;
        return landing;
    }

    private static BlockPos searchIsland(ServerLevel level) {
        var generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        int emptyHeight = level.getMinY() + 1;

        for (int radius = 0; radius <= SEARCH_RADIUS; radius += SEARCH_STEP) {
            //walk the square ring at this radius
            for (int x = -radius; x <= radius; x += SEARCH_STEP) {
                for (int z = -radius; z <= radius; z += SEARCH_STEP) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                        continue;
                    }
                    int height = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                    if (height > emptyHeight) {
                        return new BlockPos(x, height, z);
                    }
                }
            }
        }
        return null;
    }

    private static void placePlatform(ServerLevel level, BlockPos centre) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlockAndUpdate(centre.offset(dx, 0, dz), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
    }
}
