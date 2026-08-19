package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


public final class AscBiomeBuilder {
    private AscBiomeBuilder() {
    }

    // Mountains
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

    // Forests
    public static Biome ancientGrove(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.darkForest(placedFeatures, carvers, false);
    }

    public static Biome jadebloomForest(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.forest(placedFeatures, carvers, false, false, true);
    }

    public static Biome mistyWoods(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.oldGrowthTaiga(placedFeatures, carvers, true);
    }

    // Open country
    public static Biome verdantMoor(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.plains(placedFeatures, carvers, false, false, false);
    }

    public static Biome goldenSteppe(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return OverworldBiomes.savanna(placedFeatures, carvers, false, false);
    }
}
