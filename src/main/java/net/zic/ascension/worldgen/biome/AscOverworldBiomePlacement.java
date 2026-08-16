package net.zic.ascension.worldgen.biome;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class AscOverworldBiomePlacement {
    private AscOverworldBiomePlacement() {
    }

    public static ResourceKey<Biome> remapSurfaceBiome(ResourceKey<Biome> vanillaBiome) {
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
