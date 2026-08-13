package net.zic.ascension.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;

public class AscBiomeModifier {

    //Ores
    public static final ResourceKey<BiomeModifier> ADD_JADE_ORE = registerKey(("add_jade_ore"));
    public static final ResourceKey<BiomeModifier> ADD_BLACK_IRON_ORE = registerKey(("add_black_iron_ore"));
    public static final ResourceKey<BiomeModifier> ADD_FROST_SILVER_ORE = registerKey(("add_frost_silver_ore"));



    //Trees
    public static final ResourceKey<BiomeModifier> ADD_PEACH_TREE = registerKey("add_peach_tree");


    //Herbs
    public static final ResourceKey<BiomeModifier> ADD_JADE_DEW_GRASS = registerKey("add_jade_dew_grass");
    public static final ResourceKey<BiomeModifier> ADD_GINSENG = registerKey("add_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_FIRE_GINSENG = registerKey("add_fire_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_SNOW_GINSENG = registerKey("add_snow_ginseng");
    public static final ResourceKey<BiomeModifier> ADD_LINGZHI_MUSHROOM = registerKey("add_lingzhi_mushroom");
    public static final ResourceKey<BiomeModifier> ADD_BLOOD_LINGZHI_MUSHROOM = registerKey("add_blood_lingzhi_mushroom");
    public static final ResourceKey<BiomeModifier> ADD_WHITE_JADE_ORCHID = registerKey("add_white_jade_orchid");


    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);


        //Ores
        context.register(ADD_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.JADE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_BLACK_IRON_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.BLACK_IRON_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_FROST_SILVER_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.ICE_SPIKES), biomes.getOrThrow(Biomes.FROZEN_PEAKS), biomes.getOrThrow(Biomes.SNOWY_PLAINS), biomes.getOrThrow(Biomes.SNOWY_TAIGA), biomes.getOrThrow(Biomes.SNOWY_SLOPES), biomes.getOrThrow(Biomes.SNOWY_BEACH), biomes.getOrThrow(Biomes.FROZEN_OCEAN), biomes.getOrThrow(Biomes.FROZEN_RIVER), biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.FROST_SILVER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));


        //Herbs
        context.register(ADD_JADE_DEW_GRASS, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_FOREST),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.JADE_DEW_GRASS_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS), biomes.getOrThrow(Biomes.FOREST), biomes.getOrThrow(Biomes.BIRCH_FOREST), biomes.getOrThrow(Biomes.DARK_FOREST), biomes.getOrThrow(Biomes.BIRCH_FOREST)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_FIRE_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.BEACH), biomes.getOrThrow(Biomes.DESERT)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.FIRE_GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_SNOW_GINSENG, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SNOWY_TAIGA), biomes.getOrThrow(Biomes.SNOWY_BEACH), biomes.getOrThrow(Biomes.SNOWY_SLOPES), biomes.getOrThrow(Biomes.SNOWY_PLAINS)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.SNOW_GINSENG_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_WHITE_JADE_ORCHID, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.DARK_FOREST), biomes.getOrThrow(Biomes.PALE_GARDEN)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.WHITE_JADE_ORCHID_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_LINGZHI_MUSHROOM, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_FOREST),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.LINGZHI_MUSHROOM_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_BLOOD_LINGZHI_MUSHROOM, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SOUL_SAND_VALLEY)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.BLOOD_LINGZHI_MUSHROOM_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));



        //Trees
        context.register(ADD_PEACH_TREE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.PLAINS)),
                HolderSet.direct(placedFeatures.getOrThrow(AscPlacedFeatures.PEACH_TREE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));



    }








    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }
}
