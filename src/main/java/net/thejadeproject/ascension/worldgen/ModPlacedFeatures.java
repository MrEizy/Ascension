package net.thejadeproject.ascension.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;

import java.util.List;

import static net.thejadeproject.ascension.worldgen.ModOrePlacement.commonOrePlacement;
import static net.thejadeproject.ascension.worldgen.ModOrePlacement.rareOrePlacement;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> JADE_ORE_PLACED_KEY = registerKey("jade_ore");
    public static final ResourceKey<PlacedFeature> BLACK_IRON_ORE_PLACED_KEY = registerKey("black_iron_ore_placed");
    public static final ResourceKey<PlacedFeature> FROST_SILVER_ORE_PLACED_KEY = registerKey("frost_silver_ore_placed");

    public static final ResourceKey<PlacedFeature> GOLDEN_PALM_PLACED_KEY = registerKey("golden_palm_placed");

    public static final ResourceKey<PlacedFeature> ORE_MARBLE_UPPER = registerKey("ore_marble_upper");
    public static final ResourceKey<PlacedFeature> ORE_MARBLE_LOWER = registerKey("ore_marble_lower");







    // ── Wild Herbs ────────────────────────────────────────────────────────────
    /** Rarity: once every ~48 chunks. Adjust the RarityFilter value to tune spawn rate. */
    public static final ResourceKey<PlacedFeature> WILD_GINSENG_PLACED_KEY =
            registerKey("wild_hundred_year_ginseng_placed");
    public static final ResourceKey<PlacedFeature> WILD_SNOW_GINSENG_PLACED_KEY =
            registerKey("wild_hundred_year_snow_ginseng_placed");
    public static final ResourceKey<PlacedFeature> WILD_FIRE_GINSENG_PLACED_KEY =
            registerKey("wild_hundred_year_fire_ginseng_placed");
    public static final ResourceKey<PlacedFeature> WILD_WHITE_JADE_ORCHID_PLACED_KEY =
            registerKey("wild_white_jade_orchid_placed");
    public static final ResourceKey<PlacedFeature> WILD_JADE_DEW_GRASS_PLACED_KEY =
            registerKey("wild_jade_dew_grass_placed");






    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> holder9 = holdergetter.getOrThrow(ModConfiguredFeatures.ORE_MARBLE);

        register(context, ORE_MARBLE_UPPER, holder9, rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        register(context, ORE_MARBLE_LOWER, holder9, commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));


        register(context, JADE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_JADE_ORE_KEY),
            commonOrePlacement(5, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0))));

        register(context, BLACK_IRON_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_BLACK_IRON_ORE_KEY),
            commonOrePlacement(5, HeightRangePlacement.triangle(VerticalAnchor.absolute(10), VerticalAnchor.absolute(120))));
        register(context, FROST_SILVER_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_FROST_SILVER_ORE_KEY),
            commonOrePlacement(5, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0))));


        register(context, GOLDEN_PALM_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GOLDEN_PALM_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1f, 1),
                        ModBlocks.GOLDEN_PALM_SAPLING.get()));
//        register(context, IRONWOOD_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.IRONWOOD_KEY),
//                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.1f, 1),
//                        ModBlocks.IRONWOOD_SAPLING.get()));




        // ── Wild Herbs ────────────────────────────────────────────────────────
        // Each herb uses the same placement modifiers pattern:
        // CountPlacement(1) + RarityFilter(N) + InSquare + HeightRange(surface) + BiomeFilter
        // Increase RarityFilter value to make rarer; decrease to make more common.

        // Ginseng: surface y=60-120, once per ~48 chunks in biome
        register(context, WILD_GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.WILD_GINSENG_KEY),
                List.of(
                        CountPlacement.of(1),
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(60), VerticalAnchor.absolute(120)),
                        BiomeFilter.biome()));
        // Snow Ginseng: surface y=60-200 (snowy biomes tend to be higher)
        register(context, WILD_SNOW_GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.WILD_SNOW_GINSENG_KEY),
                List.of(
                        CountPlacement.of(1),
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(60), VerticalAnchor.absolute(200)),
                        BiomeFilter.biome()));
        // Fire Ginseng: surface y=30-80 (warmer/lower terrain)
        register(context, WILD_FIRE_GINSENG_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.WILD_FIRE_GINSENG_KEY),
                List.of(
                        CountPlacement.of(1),
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(120)),
                        BiomeFilter.biome()));
        // White Jade Orchid: surface y=60-120 (lush/jungle)
        register(context, WILD_WHITE_JADE_ORCHID_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.WILD_WHITE_JADE_ORCHID_KEY),
                List.of(
                        CountPlacement.of(1),
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(60), VerticalAnchor.absolute(120)),
                        BiomeFilter.biome()));
        register(context, WILD_JADE_DEW_GRASS_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.WILD_JADE_DEW_GRASS_KEY),
                List.of(
                        CountPlacement.of(2),
                        RarityFilter.onAverageOnceEvery(16),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(60), VerticalAnchor.absolute(120)),
                        BiomeFilter.biome()));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
