package net.thejadeproject.ascension.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.SpiritVeinBlock;
import net.thejadeproject.ascension.worldgen.custom.WildHerbFeatureConfig;

import java.util.List;

public class ModConfiguredFeatures {

    // ── Ores ─────────────────────────────────────────────────────────────────

    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_JADE_ORE_KEY = registerKey("jade_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_BLACK_IRON_ORE_KEY = registerKey("black_iron_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_FROST_SILVER_ORE_KEY = registerKey("frost_silver_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_MARBLE = registerKey("raw_marble");

    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLDEN_PALM_KEY = registerKey("golden_palm");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRONWOOD_KEY = registerKey("ironwood");





    // ── Wild Herbs ────────────────────────────────────────────────────────────
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_GINSENG_KEY =
            registerKey("wild_hundred_year_ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_SNOW_GINSENG_KEY =
            registerKey("wild_hundred_year_snow_ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_FIRE_GINSENG_KEY =
            registerKey("wild_hundred_year_fire_ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_WHITE_JADE_ORCHID_KEY =
            registerKey("wild_white_jade_orchid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILD_JADE_DEW_GRASS_KEY =
            registerKey("wild_jade_dew_grass");


    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest deepstoneReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest ruletest = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);


        register(context, ORE_MARBLE, Feature.ORE, new OreConfiguration(ruletest, ModBlocks.RAW_MARBLE.get().defaultBlockState(), 64));

        List<OreConfiguration.TargetBlockState> overworldJadeOres = List.of(
                OreConfiguration.target(deepstoneReplaceables, ModBlocks.JADE_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldBlackIronOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.BLACK_IRON_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldFrostSilverOres = List.of(
                OreConfiguration.target(deepstoneReplaceables, ModBlocks.FROST_SILVER_ORE.get().defaultBlockState()));

        register(context, OVERWORLD_JADE_ORE_KEY, Feature.ORE, new OreConfiguration(overworldJadeOres, 4));
        register(context, OVERWORLD_BLACK_IRON_ORE_KEY, Feature.ORE, new OreConfiguration(overworldBlackIronOres, 4));
        register(context, OVERWORLD_FROST_SILVER_ORE_KEY, Feature.ORE, new OreConfiguration(overworldFrostSilverOres, 4));


        register(context, GOLDEN_PALM_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.GOLDEN_PALM_LOG.get()),
                new ForkingTrunkPlacer(4, 4, 3),

                BlockStateProvider.simple(ModBlocks.GOLDEN_PALM_LEAVES.get()),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(3), 3),

                new TwoLayersFeatureSize(1, 0, 2)).dirt(BlockStateProvider.simple(Blocks.SAND)).build());

        register(context, IRONWOOD_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.IRONWOOD_LOG.get()),
                new DarkOakTrunkPlacer(
                        6,  // baseHeight - dark oak trunk height
                        2,  // heightRandA - height variation
                        0   // heightRandB - additional variation
                ),
                BlockStateProvider.simple(ModBlocks.IRONWOOD_LEAVES.get()),
                new DarkOakFoliagePlacer(
                        ConstantInt.of(0),  // radius
                        ConstantInt.of(0)   // offset
                ),
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .dirt(BlockStateProvider.simple(Blocks.GRASS_BLOCK))
                .build());





        // ── Wild Herbs ────────────────────────────────────────────────────────
        register(context, WILD_GINSENG_KEY,
                ModFeatureRegistration.WILD_HERB_FEATURE.get(),
                new WildHerbFeatureConfig(
                        ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get(),
                        List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL,
                                Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT)));
        register(context, WILD_SNOW_GINSENG_KEY,
                ModFeatureRegistration.WILD_HERB_FEATURE.get(),
                new WildHerbFeatureConfig(
                        ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get(),
                        List.of(Blocks.SNOW_BLOCK, Blocks.ICE, Blocks.PACKED_ICE,
                                Blocks.BLUE_ICE, Blocks.POWDER_SNOW)));
        register(context, WILD_FIRE_GINSENG_KEY,
                ModFeatureRegistration.WILD_HERB_FEATURE.get(),
                new WildHerbFeatureConfig(
                        ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get(),
                        List.of(Blocks.NETHERRACK, Blocks.MAGMA_BLOCK,
                                Blocks.BASALT, Blocks.BLACKSTONE)));
        register(context, WILD_WHITE_JADE_ORCHID_KEY,
                ModFeatureRegistration.WILD_HERB_FEATURE.get(),
                new WildHerbFeatureConfig(
                        ModBlocks.WHITE_JADE_ORCHID_CROP.get(),
                        List.of(Blocks.GRASS_BLOCK, Blocks.MOSS_BLOCK,
                                Blocks.MOSS_CARPET, Blocks.DIRT)));
        register(context, WILD_JADE_DEW_GRASS_KEY,
                ModFeatureRegistration.WILD_HERB_FEATURE.get(),
                new WildHerbFeatureConfig(
                        ModBlocks.JADE_DEW_GRASS_CROP.get(),
                        List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT,
                                Blocks.ROOTED_DIRT, Blocks.PODZOL)));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
