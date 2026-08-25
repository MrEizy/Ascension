package net.zic.ascension.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.worldgen.features.HerbFeature;
import net.zic.ascension.worldgen.features.LingzhiMushroomConfiguration;
import net.zic.ascension.worldgen.features.SurfaceRockFeature;
import net.zic.ascension.worldgen.features.SurfaceScatterFeature;
import net.zic.ascension.worldgen.tree.PodCropDecorator;

import java.util.List;

public class AscConfiguredFeatures {

    // Ores
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_JADE_ORE_KEY = registerKey("jade_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_BLACK_IRON_ORE_KEY = registerKey("black_iron_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_FROST_SILVER_ORE_KEY = registerKey("frost_silver_ore");

    // Trees
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEACH_TREE_KEY = registerKey("peach_tree_key");

    // Herbs
    public static final ResourceKey<ConfiguredFeature<?, ?>> JADE_DEW_GRASS = registerKey("jade_dew_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GINSENG = registerKey("ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FIRE_GINSENG = registerKey("fire_ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SNOW_GINSENG = registerKey("snow_ginseng");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WHITE_JADE_ORCHID = registerKey("white_jade_orchid");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NINE_SUN_FIRE_ROOT = registerKey("nine_sun_fire_root");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOONWELL_JADE_LOTUS = registerKey("moonwell_jade_lotus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LINGZHI_MUSHROOM = registerKey("lingzhi_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BLOOD_LINGZHI_MUSHROOM = registerKey("blood_lingzhi_mushroom");

    // rock clusters
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREYSTONE_ROCKS = registerKey("greystone_rocks");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AZURE_CLOUD_ROCKS = registerKey("azure_cloud_rocks");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HEAVENREACH_CRAGS = registerKey("heavenreach_crags");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDANT_MOOR_STONES = registerKey("verdant_moor_stones");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLDEN_STEPPE_STONES = registerKey("golden_steppe_stones");

    // vanilla floor flora (would like to swap with custom plants and things later obviously)
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREYSTONE_FLOOR_SCATTER = registerKey("greystone_floor_scatter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_GROVE_FLOOR_SCATTER = registerKey("ancient_grove_floor_scatter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> JADEBLOOM_FLOOR_SCATTER = registerKey("jadebloom_floor_scatter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MISTY_WOODS_FLOOR_SCATTER = registerKey("misty_woods_floor_scatter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDANT_MOOR_FLOOR_SCATTER = registerKey("verdant_moor_floor_scatter");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLDEN_STEPPE_FLOOR_SCATTER = registerKey("golden_steppe_floor_scatter");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var blocks = context.lookup(Registries.BLOCK);
        RuleTest deepstoneReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> overworldJadeOres = List.of(
                OreConfiguration.target(deepstoneReplaceables, ModBlocks.JADE_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldBlackIronOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.BLACK_IRON_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> overworldFrostSilverOres = List.of(
                OreConfiguration.target(deepstoneReplaceables, ModBlocks.FROST_SILVER_ORE.get().defaultBlockState()));

        register(context, PEACH_TREE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.PEACH_LOG.get()),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.simple(ModBlocks.PEACH_LEAVES.get()),
                new BlobFoliagePlacer(
                        ConstantInt.of(2),
                        ConstantInt.of(0),
                        3),
                new TwoLayersFeatureSize(1, 0, 2))
                .ignoreVines()
                .decorators(List.of(new PodCropDecorator(0.15f)))
                .build());

        // Ores
        register(context, OVERWORLD_JADE_ORE_KEY, Feature.ORE, new OreConfiguration(overworldJadeOres, 4));
        register(context, OVERWORLD_BLACK_IRON_ORE_KEY, Feature.ORE, new OreConfiguration(overworldBlackIronOres, 4));
        register(context, OVERWORLD_FROST_SILVER_ORE_KEY, Feature.ORE, new OreConfiguration(overworldFrostSilverOres, 4));

        // Herbs
        register(context, JADE_DEW_GRASS, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.JADE_DEW_GRASS_CROP.get(), 6, 3));
        register(context, GINSENG, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.GINSENG_CROP.get(), 4, 3));
        register(context, FIRE_GINSENG, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.FIRE_GINSENG_CROP.get(), 4, 3));
        register(context, SNOW_GINSENG, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.SNOW_GINSENG_CROP.get(), 4, 3));
        register(context, WHITE_JADE_ORCHID, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.WHITE_JADE_ORCHID_CROP.get(), 4, 3));
        register(context, NINE_SUN_FIRE_ROOT, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.NINE_SUN_FIRE_ROOT_CROP.get(), 5, 4));
        register(context, MOONWELL_JADE_LOTUS, AscFeatures.HERB.get(),
                new HerbFeature.Configuration(ModBlocks.MOONWELL_JADE_LOTUS_CROP.get(), 5, 4));
        register(context, LINGZHI_MUSHROOM, AscFeatures.LINGZHI_MUSHROOM.get(),
                new LingzhiMushroomConfiguration(ModBlocks.LINGZHI_MUSHROOM_B.get(), blocks.getOrThrow(BlockTags.LOGS)));
        register(context, BLOOD_LINGZHI_MUSHROOM, AscFeatures.LINGZHI_MUSHROOM.get(),
                new LingzhiMushroomConfiguration(ModBlocks.BLOOD_LINGZHI_MUSHROOM_B.get(),
                        HolderSet.direct(Blocks.BONE_BLOCK.builtInRegistryHolder())));

        // surface rocks
        register(context, GREYSTONE_ROCKS, AscFeatures.SURFACE_ROCK.get(),
                new SurfaceRockFeature.Configuration(
                        List.of(Blocks.ANDESITE, Blocks.STONE, Blocks.MOSSY_COBBLESTONE), 1, 2, 2));
        register(context, AZURE_CLOUD_ROCKS, AscFeatures.SURFACE_ROCK.get(),
                new SurfaceRockFeature.Configuration(
                        List.of(Blocks.STONE, Blocks.ANDESITE, Blocks.MOSSY_COBBLESTONE), 1, 2, 2));
        register(context, HEAVENREACH_CRAGS, AscFeatures.SURFACE_ROCK.get(),
                new SurfaceRockFeature.Configuration(
                        List.of(Blocks.STONE, Blocks.TUFF, Blocks.CALCITE), 2, 3, 3));
        register(context, VERDANT_MOOR_STONES, AscFeatures.SURFACE_ROCK.get(),
                new SurfaceRockFeature.Configuration(
                        List.of(Blocks.STONE, Blocks.ANDESITE, Blocks.MOSSY_COBBLESTONE), 1, 2, 1));
        register(context, GOLDEN_STEPPE_STONES, AscFeatures.SURFACE_ROCK.get(),
                new SurfaceRockFeature.Configuration(
                        List.of(Blocks.STONE, Blocks.ANDESITE, Blocks.TUFF), 1, 2, 2));

        // Placeholder plants until we add more custom ones
        register(context, GREYSTONE_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.FERN, Blocks.MOSS_CARPET), 18, 6));
        register(context, ANCIENT_GROVE_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.MOSS_CARPET, Blocks.FERN, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM), 30, 7));
        register(context, JADEBLOOM_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.PINK_PETALS, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY), 30, 7));
        register(context, MISTY_WOODS_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.MOSS_CARPET, Blocks.FERN, Blocks.BROWN_MUSHROOM), 28, 7));
        register(context, VERDANT_MOOR_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.DANDELION, Blocks.POPPY, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET), 22, 7));
        register(context, GOLDEN_STEPPE_FLOOR_SCATTER, AscFeatures.SURFACE_SCATTER.get(),
                new SurfaceScatterFeature.Configuration(
                        List.of(Blocks.DEAD_BUSH), 10, 7));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration
    ) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
