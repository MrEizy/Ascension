package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class AscBiomeBuilder {
    private AscBiomeBuilder() {
    }

    public static Biome greystoneFoothills(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.windsweptHills(placedFeatures, carvers, true);
    }

    public static Biome azureCloudRange(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.grove(placedFeatures, carvers);
    }

    public static Biome heavenreachPeaks(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.jaggedPeaks(placedFeatures, carvers);
    }
}
