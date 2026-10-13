package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class AscBiomeBuilder {
    private AscBiomeBuilder() {
    }

    public static Biome greystoneFoothills(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.windsweptHills(placedFeatures, carvers, true),
                AscBiomeFeatures.greystoneFoothills(placedFeatures, carvers)
        );
    }

    public static Biome azureCloudRange(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.grove(placedFeatures, carvers),
                AscBiomeFeatures.azureCloudRange(placedFeatures, carvers)
        );
    }

    public static Biome heavenreachPeaks(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.jaggedPeaks(placedFeatures, carvers),
                AscBiomeFeatures.heavenreachPeaks(placedFeatures, carvers)
        );
    }

    public static Biome ancientGrove(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.darkForest(placedFeatures, carvers, false),
                AscBiomeFeatures.ancientGrove(placedFeatures, carvers)
        );
    }

    public static Biome jadebloomForest(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.forest(placedFeatures, carvers, false, false, true),
                AscBiomeFeatures.jadebloomForest(placedFeatures, carvers)
        );
    }

    public static Biome mistyWoods(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.oldGrowthTaiga(placedFeatures, carvers, true),
                AscBiomeFeatures.mistyWoods(placedFeatures, carvers)
        );
    }

    public static Biome verdantMoor(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.plains(placedFeatures, carvers, false, false, false),
                AscBiomeFeatures.verdantMoor(placedFeatures, carvers)
        );
    }

    public static Biome goldenSteppe(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        return rebuild(
                OverworldBiomes.savanna(placedFeatures, carvers, false, false),
                AscBiomeFeatures.goldenSteppe(placedFeatures, carvers)
        );
    }

    private static Biome rebuild(Biome template, BiomeGenerationSettings generationSettings) {
        Biome.ClimateSettings climate = template.getModifiedClimateSettings();
        return new Biome.BiomeBuilder()
                .hasPrecipitation(climate.hasPrecipitation())
                .temperature(climate.temperature())
                .temperatureAdjustment(climate.temperatureModifier())
                .downfall(climate.downfall())
                .putAttributes(template.getAttributes())
                .specialEffects(template.getSpecialEffects())
                .mobSpawnSettings(template.getMobSettings())
                .generationSettings(generationSettings)
                .build();
    }
}
