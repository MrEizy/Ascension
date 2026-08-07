package net.zic.ascension.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.zic.ascension.AscensionCraft;

import java.util.List;

public class AscPlacedFeatures {


    //Ores
    public static final ResourceKey<PlacedFeature> JADE_ORE_PLACED_KEY = registerKey("jade_ore");
    public static final ResourceKey<PlacedFeature> BLACK_IRON_ORE_PLACED_KEY = registerKey("black_iron_ore_placed");
    public static final ResourceKey<PlacedFeature> FROST_SILVER_ORE_PLACED_KEY = registerKey("frost_silver_ore_placed");


    //Herbs
    public static final ResourceKey<PlacedFeature> LINGZHI_MUSHROOM_PLACED_KEY = registerKey("lingzhi_mushroom_placed");
    public static final ResourceKey<PlacedFeature> BLOOD_LINGZHI_MUSHROOM_PLACED_KEY = registerKey("blood_lingzhi_mushroom_placed");


    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        //Ores

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




        //Herbs
        register(context, LINGZHI_MUSHROOM_PLACED_KEY,
                configuredFeatures.getOrThrow(AscConfiguredFeatures.LINGZHI_MUSHROOM),
                List.of(
                        CountPlacement.of(8),              // Only 2 scan-attempts per chunk
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
                        CountPlacement.of(10),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(0),
                                VerticalAnchor.absolute(200)
                        ),
                        BiomeFilter.biome()
                ));
    }

    public static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightRange) {
        return orePlacement(CountPlacement.of(count), heightRange);
    }
    public static List<PlacementModifier> orePlacement(PlacementModifier frequencyModifier, PlacementModifier heightRange) {
        return List.of(frequencyModifier, InSquarePlacement.spread(), heightRange, BiomeFilter.biome());
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
