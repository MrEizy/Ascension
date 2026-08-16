package net.zic.ascension.worldgen.biome;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class AscOverworldBiomePlacement {
    private AscOverworldBiomePlacement() {
    }

    public static ResourceKey<Biome> remapSurfaceBiome(ResourceKey<Biome> vanillaBiome) {
        // Mountains
        if (isAny(vanillaBiome,
                Biomes.MEADOW,
                Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_FOREST,
                Biomes.WINDSWEPT_GRAVELLY_HILLS)) {
            return AscBiomes.GREYSTONE_FOOTHILLS;
        }

        if (isAny(vanillaBiome,
                Biomes.GROVE,
                Biomes.SNOWY_SLOPES)) {
            return AscBiomes.AZURE_CLOUD_RANGE;
        }

        if (isAny(vanillaBiome,
                Biomes.JAGGED_PEAKS,
                Biomes.FROZEN_PEAKS,
                Biomes.STONY_PEAKS)) {
            return AscBiomes.HEAVENREACH_PEAKS;
        }

        // Forests
        if (isAny(vanillaBiome,
                Biomes.DARK_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST)) {
            return AscBiomes.ANCIENT_GROVE;
        }

        if (isAny(vanillaBiome,
                Biomes.FOREST,
                Biomes.FLOWER_FOREST,
                Biomes.BIRCH_FOREST)) {
            return AscBiomes.JADEBLOOM_FOREST;
        }

        if (isAny(vanillaBiome,
                Biomes.TAIGA,
                Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA)) {
            return AscBiomes.MISTY_WOODS;
        }

        // Open-countries
        if (isAny(vanillaBiome,
                Biomes.PLAINS,
                Biomes.SUNFLOWER_PLAINS)) {
            return AscBiomes.VERDANT_MOOR;
        }

        if (isAny(vanillaBiome,
                Biomes.SAVANNA,
                Biomes.SAVANNA_PLATEAU,
                Biomes.WINDSWEPT_SAVANNA)) {
            return AscBiomes.GOLDEN_STEPPE;
        }

        return vanillaBiome;
    }

    @SafeVarargs
    private static boolean isAny(ResourceKey<Biome> value, ResourceKey<Biome>... candidates) {
        for (ResourceKey<Biome> candidate : candidates) {
            if (candidate.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
