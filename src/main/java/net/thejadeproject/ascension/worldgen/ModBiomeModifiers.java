package net.thejadeproject.ascension.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.entity.ModEntities;

import java.util.List;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_JADE_ORE = registerKey(("add_jade_ore"));
    public static final ResourceKey<BiomeModifier> ADD_BLACK_IRON_ORE = registerKey(("add_black_iron_ore"));
    public static final ResourceKey<BiomeModifier> ADD_FROST_SILVER_ORE = registerKey(("add_frost_silver_ore"));

    public static final ResourceKey<BiomeModifier> ADD_GOLDEN_PALM = registerKey("add_golden_palm");
//    public static final ResourceKey<BiomeModifier> ADD_IRONWOOD = registerKey("add_ironwood");
    public static final ResourceKey<BiomeModifier> ADD_RAW_MARBLE = registerKey("add_raw_marble");



    // ── Wild Herbs ────────────────────────────────────────────────────────────
    public static final ResourceKey<BiomeModifier> ADD_WILD_GINSENG = registerKey("add_wild_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_WILD_SNOW_GINSENG = registerKey("add_wild_snow_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_WILD_FIRE_GINSENG = registerKey("add_wild_fire_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_WILD_WHITE_JADE_ORCHID = registerKey("add_wild_white_jade_orchid");
    public static final ResourceKey<BiomeModifier> ADD_WILD_JADE_DEW_GRASS = registerKey("add_wild_jade_dew_grass");


    public static final ResourceKey<BiomeModifier> SPAWN_RAT = registerKey("spawn_rat");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.JADE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_BLACK_IRON_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.BLACK_IRON_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_FROST_SILVER_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.ICE_SPIKES), biomes.getOrThrow(Biomes.FROZEN_PEAKS), biomes.getOrThrow(Biomes.SNOWY_PLAINS), biomes.getOrThrow(Biomes.SNOWY_TAIGA), biomes.getOrThrow(Biomes.SNOWY_SLOPES), biomes.getOrThrow(Biomes.SNOWY_BEACH), biomes.getOrThrow(Biomes.FROZEN_OCEAN), biomes.getOrThrow(Biomes.FROZEN_RIVER), biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.FROST_SILVER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_RAW_MARBLE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ORE_MARBLE_LOWER), placedFeatures.getOrThrow(ModPlacedFeatures.ORE_MARBLE_UPPER)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_GOLDEN_PALM, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.BEACH)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.GOLDEN_PALM_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(SPAWN_RAT, new BiomeModifiers.AddSpawnsBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.FOREST), biomes.getOrThrow(Biomes.BIRCH_FOREST)),
                        List.of(new MobSpawnSettings.SpawnerData(ModEntities.RAT.get(), 10, 2, 4))));




        // ── Wild Herbs ────────────────────────────────────────────────────────

        context.register(ADD_WILD_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.FOREST),
                        biomes.getOrThrow(Biomes.BIRCH_FOREST),
                        biomes.getOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST),
                        biomes.getOrThrow(Biomes.DARK_FOREST),
                        biomes.getOrThrow(Biomes.TAIGA),
                        biomes.getOrThrow(Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                        biomes.getOrThrow(Biomes.OLD_GROWTH_PINE_TAIGA)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.WILD_GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_WILD_SNOW_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.SNOWY_PLAINS),
                        biomes.getOrThrow(Biomes.SNOWY_TAIGA),
                        biomes.getOrThrow(Biomes.SNOWY_SLOPES),
                        biomes.getOrThrow(Biomes.SNOWY_BEACH),
                        biomes.getOrThrow(Biomes.ICE_SPIKES),
                        biomes.getOrThrow(Biomes.FROZEN_PEAKS),
                        biomes.getOrThrow(Biomes.JAGGED_PEAKS)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.WILD_SNOW_GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_WILD_FIRE_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.SAVANNA),
                        biomes.getOrThrow(Biomes.SAVANNA_PLATEAU),
                        biomes.getOrThrow(Biomes.WINDSWEPT_SAVANNA),
                        biomes.getOrThrow(Biomes.BADLANDS),
                        biomes.getOrThrow(Biomes.ERODED_BADLANDS),
                        biomes.getOrThrow(Biomes.WOODED_BADLANDS)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.WILD_FIRE_GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_WILD_WHITE_JADE_ORCHID, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.JUNGLE),
                        biomes.getOrThrow(Biomes.SPARSE_JUNGLE),
                        biomes.getOrThrow(Biomes.BAMBOO_JUNGLE),
                        biomes.getOrThrow(Biomes.LUSH_CAVES)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.WILD_WHITE_JADE_ORCHID_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_WILD_JADE_DEW_GRASS, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(
                        biomes.getOrThrow(Biomes.PLAINS),
                        biomes.getOrThrow(Biomes.MEADOW),
                        biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS),
                        biomes.getOrThrow(Biomes.FOREST),
                        biomes.getOrThrow(Biomes.BIRCH_FOREST),
                        biomes.getOrThrow(Biomes.FLOWER_FOREST)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.WILD_JADE_DEW_GRASS_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));



    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }
}
