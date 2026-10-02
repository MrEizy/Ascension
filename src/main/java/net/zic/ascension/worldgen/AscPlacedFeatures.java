package net.zic.ascension.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.List;

public class AscPlacedFeatures {

    // Ores
    public static final ResourceKey<PlacedFeature> JADE_ORE_PLACED_KEY = registerKey("jade_ore");
    public static final ResourceKey<PlacedFeature> BLACK_IRON_ORE_PLACED_KEY = registerKey("black_iron_ore_placed");
    public static final ResourceKey<PlacedFeature> FROST_SILVER_ORE_PLACED_KEY = registerKey("frost_silver_ore_placed");

    // Trees
    public static final ResourceKey<PlacedFeature> PEACH_TREE_PLACED_KEY = registerKey("peach_tree_placed");

    // Herbs
    public static final ResourceKey<PlacedFeature> JADE_DEW_GRASS_PLACED_KEY = registerKey("jade_dew_grass_placed");
    public static final ResourceKey<PlacedFeature> GINSENG_PLACED_KEY = registerKey("ginseng_placed");
    public static final ResourceKey<PlacedFeature> FIRE_GINSENG_PLACED_KEY = registerKey("fire_ginseng_placed");
    public static final ResourceKey<PlacedFeature> SNOW_GINSENG_PLACED_KEY = registerKey("snow_ginseng_placed");
    public static final ResourceKey<PlacedFeature> WHITE_JADE_ORCHID_PLACED_KEY = registerKey("white_jade_orchid_placed");
    public static final ResourceKey<PlacedFeature> NINE_SUN_FIRE_ROOT_PLACED_KEY = registerKey("nine_sun_fire_root_placed");
    public static final ResourceKey<PlacedFeature> MOONWELL_JADE_LOTUS_PLACED_KEY = registerKey("moonwell_jade_lotus_placed");
    public static final ResourceKey<PlacedFeature> LINGZHI_MUSHROOM_PLACED_KEY = registerKey("lingzhi_mushroom_placed");
    public static final ResourceKey<PlacedFeature> BLOOD_LINGZHI_MUSHROOM_PLACED_KEY = registerKey("blood_lingzhi_mushroom_placed");

    // rocks
    public static final ResourceKey<PlacedFeature> GREYSTONE_ROCKS_PLACED = registerKey("greystone_rocks");
    public static final ResourceKey<PlacedFeature> AZURE_CLOUD_ROCKS_PLACED = registerKey("azure_cloud_rocks");
    public static final ResourceKey<PlacedFeature> HEAVENREACH_CRAGS_PLACED = registerKey("heavenreach_crags");
    public static final ResourceKey<PlacedFeature> VERDANT_MOOR_STONES_PLACED = registerKey("verdant_moor_stones");
    public static final ResourceKey<PlacedFeature> GOLDEN_STEPPE_STONES_PLACED = registerKey("golden_steppe_stones");

    // floor scatter
    public static final ResourceKey<PlacedFeature> GREYSTONE_FLOOR_SCATTER_PLACED = registerKey("greystone_floor_scatter");
    public static final ResourceKey<PlacedFeature> ANCIENT_GROVE_FLOOR_SCATTER_PLACED = registerKey("ancient_grove_floor_scatter");
    public static final ResourceKey<PlacedFeature> JADEBLOOM_FLOOR_SCATTER_PLACED = registerKey("jadebloom_floor_scatter");
    public static final ResourceKey<PlacedFeature> MISTY_WOODS_FLOOR_SCATTER_PLACED = registerKey("misty_woods_floor_scatter");
    public static final ResourceKey<PlacedFeature> VERDANT_MOOR_FLOOR_SCATTER_PLACED = registerKey("verdant_moor_floor_scatter");
    public static final ResourceKey<PlacedFeature> GOLDEN_STEPPE_FLOOR_SCATTER_PLACED = registerKey("golden_steppe_floor_scatter");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Ores
        register(context, JADE_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.OVERWORLD_JADE_ORE_KEY),
                commonOrePlacement(5,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(0)
                        )));
        register(context, FROST_SILVER_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.OVERWORLD_FROST_SILVER_ORE_KEY),
                commonOrePlacement(5,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(0)
                        )));
        register(context, BLACK_IRON_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.OVERWORLD_BLACK_IRON_ORE_KEY),
                commonOrePlacement(5,
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(10),
                                VerticalAnchor.absolute(120)
                        )));

        // Trees
        register(context, PEACH_TREE_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.PEACH_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(3, 0.1f, 2),
                        ModBlocks.PEACH_SAPLING.get()
                ));

        // Herbs
        register(context, JADE_DEW_GRASS_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.JADE_DEW_GRASS),
                List.of(CountPlacement.of(12), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.GINSENG),
                List.of(CountPlacement.of(6), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, FIRE_GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.FIRE_GINSENG),
                List.of(CountPlacement.of(6), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, SNOW_GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.SNOW_GINSENG),
                List.of(CountPlacement.of(6), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, WHITE_JADE_ORCHID_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.WHITE_JADE_ORCHID),
                List.of(CountPlacement.of(6), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, NINE_SUN_FIRE_ROOT_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.NINE_SUN_FIRE_ROOT),
                List.of(CountPlacement.of(8), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, MOONWELL_JADE_LOTUS_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.MOONWELL_JADE_LOTUS),
                List.of(CountPlacement.of(8), InSquarePlacement.spread(), BiomeFilter.biome()));

        register(context, LINGZHI_MUSHROOM_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.LINGZHI_MUSHROOM),
                List.of(
                        CountPlacement.of(8),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(58),
                                VerticalAnchor.absolute(95)
                        ),
                        BiomeFilter.biome()
                ));

        register(context, BLOOD_LINGZHI_MUSHROOM_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.BLOOD_LINGZHI_MUSHROOM),
                List.of(
                        CountPlacement.of(14),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(0),
                                VerticalAnchor.absolute(200)
                        ),
                        BiomeFilter.biome()
                ));

        // sparse rock formations
        register(context, GREYSTONE_ROCKS_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.GREYSTONE_ROCKS),
                surfaceRarity(5));
        register(context, AZURE_CLOUD_ROCKS_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.AZURE_CLOUD_ROCKS),
                surfaceRarity(7));
        register(context, HEAVENREACH_CRAGS_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.HEAVENREACH_CRAGS),
                surfaceRarity(4));
        register(context, VERDANT_MOOR_STONES_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.VERDANT_MOOR_STONES),
                surfaceRarity(12));
        register(context, GOLDEN_STEPPE_STONES_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.GOLDEN_STEPPE_STONES),
                surfaceRarity(9));

        // floor detail
        register(context, GREYSTONE_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.GREYSTONE_FLOOR_SCATTER),
                surfaceCount(1));
        register(context, ANCIENT_GROVE_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.ANCIENT_GROVE_FLOOR_SCATTER),
                surfaceCount(2));
        register(context, JADEBLOOM_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.JADEBLOOM_FLOOR_SCATTER),
                surfaceCount(3));
        register(context, MISTY_WOODS_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.MISTY_WOODS_FLOOR_SCATTER),
                surfaceCount(2));
        register(context, VERDANT_MOOR_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.VERDANT_MOOR_FLOOR_SCATTER),
                surfaceCount(2));
        register(context, GOLDEN_STEPPE_FLOOR_SCATTER_PLACED,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.GOLDEN_STEPPE_FLOOR_SCATTER),
                surfaceCount(1));
    }

    private static List<PlacementModifier> surfaceRarity(int averageChunks) {
        return List.of(
                RarityFilter.onAverageOnceEvery(averageChunks),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    private static List<PlacementModifier> surfaceCount(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightRange) {
        return orePlacement(CountPlacement.of(count), heightRange);
    }

    public static List<PlacementModifier> orePlacement(PlacementModifier frequencyModifier, PlacementModifier heightRange) {
        return List.of(frequencyModifier, InSquarePlacement.spread(), heightRange, BiomeFilter.biome());
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers
    ) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
