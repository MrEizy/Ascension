package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.zic.ascension.worldgen.AscPlacedFeatures;

public final class AscBiomeFeatures {
    private AscBiomeFeatures() {
    }

    public static BiomeGenerationSettings greystoneFoothills(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "trees_windswept_forest",
                "patch_bush",
                "flower_default",
                "patch_grass_badlands",
                "brown_mushroom_normal",
                "red_mushroom_normal"
        );
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AscPlacedFeatures.GREYSTONE_ROCKS_PLACED);
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.GREYSTONE_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings azureCloudRange(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "trees_grove",
                "patch_large_fern",
                "patch_grass_taiga",
                "brown_mushroom_old_growth",
                "red_mushroom_old_growth",
                "patch_berry_common"
        );
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AscPlacedFeatures.AZURE_CLOUD_ROCKS_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings heavenreachPeaks(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AscPlacedFeatures.HEAVENREACH_CRAGS_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings ancientGrove(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "dark_forest_vegetation",
                "forest_flowers",
                "patch_grass_forest",
                "patch_large_fern",
                "brown_mushroom_normal",
                "red_mushroom_normal",
                "patch_leaf_litter"
        );
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.ANCIENT_GROVE_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings jadebloomForest(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "flower_forest_flowers",
                "trees_flower_forest",
                "flower_flower_forest",
                "patch_bush",
                "patch_grass_forest",
                "brown_mushroom_normal",
                "red_mushroom_normal"
        );
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.JADEBLOOM_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings mistyWoods(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "patch_large_fern",
                "trees_old_growth_spruce_taiga",
                "patch_grass_taiga",
                "brown_mushroom_old_growth",
                "red_mushroom_old_growth",
                "patch_firefly_bush_near_water",
                "patch_berry_common"
        );
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.MISTY_WOODS_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings verdantMoor(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "patch_tall_grass_2",
                "trees_plains",
                "flower_plains",
                "patch_grass_plain"
        );
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AscPlacedFeatures.VERDANT_MOOR_STONES_PLACED);
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.VERDANT_MOOR_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    public static BiomeGenerationSettings goldenSteppe(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = base(placedFeatures, carvers);
        addVegetation(builder,
                "patch_tall_grass",
                "trees_savanna",
                "flower_warm",
                "patch_grass_savanna"
        );
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AscPlacedFeatures.GOLDEN_STEPPE_STONES_PLACED);
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AscPlacedFeatures.GOLDEN_STEPPE_FLOOR_SCATTER_PLACED);
        return builder.build();
    }

    private static BiomeGenerationSettings.Builder base(
            HolderGetter<PlacedFeature> placedFeatures,
            HolderGetter<ConfiguredWorldCarver<?>> carvers
    ) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addDefaultSprings(builder);
        return builder;
    }

    private static void addVegetation(BiomeGenerationSettings.Builder builder, String... features) {
        for (String feature : features) {
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, vanillaFeature(feature));
        }
    }

    private static ResourceKey<PlacedFeature> vanillaFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("minecraft", path));
    }
}
