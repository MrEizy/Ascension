package net.zic.ascension.worldgen.biome;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.AscPlacedFeatures;

public final class AscBiomeFeatures {
    // Mountains
    public static final ResourceKey<BiomeModifier> GREYSTONE_ROCKS = key("biome_identity/greystone_rocks");
    public static final ResourceKey<BiomeModifier> GREYSTONE_FLOOR = key("biome_identity/greystone_floor");
    public static final ResourceKey<BiomeModifier> AZURE_CLOUD_ROCKS = key("biome_identity/azure_cloud_rocks");
    public static final ResourceKey<BiomeModifier> HEAVENREACH_CRAGS = key("biome_identity/heavenreach_crags");

    // Forests
    public static final ResourceKey<BiomeModifier> ANCIENT_GROVE_FLOOR = key("biome_identity/ancient_grove_floor");
    public static final ResourceKey<BiomeModifier> JADEBLOOM_FLOOR = key("biome_identity/jadebloom_floor");
    public static final ResourceKey<BiomeModifier> MISTY_WOODS_FLOOR = key("biome_identity/misty_woods_floor");

    // Open country
    public static final ResourceKey<BiomeModifier> VERDANT_MOOR_STONES = key("biome_identity/verdant_moor_stones");
    public static final ResourceKey<BiomeModifier> VERDANT_MOOR_FLOOR = key("biome_identity/verdant_moor_floor");
    public static final ResourceKey<BiomeModifier> GOLDEN_STEPPE_STONES = key("biome_identity/golden_steppe_stones");
    public static final ResourceKey<BiomeModifier> GOLDEN_STEPPE_FLOOR = key("biome_identity/golden_steppe_floor");

    private AscBiomeFeatures() {
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placed = context.lookup(Registries.PLACED_FEATURE);

        add(context, GREYSTONE_ROCKS, AscBiomes.GREYSTONE_FOOTHILLS,
                AscPlacedFeatures.GREYSTONE_ROCKS_PLACED, GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                biomes, placed);
        add(context, GREYSTONE_FLOOR, AscBiomes.GREYSTONE_FOOTHILLS,
                AscPlacedFeatures.GREYSTONE_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);
        add(context, AZURE_CLOUD_ROCKS, AscBiomes.AZURE_CLOUD_RANGE,
                AscPlacedFeatures.AZURE_CLOUD_ROCKS_PLACED, GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                biomes, placed);
        add(context, HEAVENREACH_CRAGS, AscBiomes.HEAVENREACH_PEAKS,
                AscPlacedFeatures.HEAVENREACH_CRAGS_PLACED, GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                biomes, placed);

        add(context, ANCIENT_GROVE_FLOOR, AscBiomes.ANCIENT_GROVE,
                AscPlacedFeatures.ANCIENT_GROVE_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);
        add(context, JADEBLOOM_FLOOR, AscBiomes.JADEBLOOM_FOREST,
                AscPlacedFeatures.JADEBLOOM_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);
        add(context, MISTY_WOODS_FLOOR, AscBiomes.MISTY_WOODS,
                AscPlacedFeatures.MISTY_WOODS_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);

        add(context, VERDANT_MOOR_STONES, AscBiomes.VERDANT_MOOR,
                AscPlacedFeatures.VERDANT_MOOR_STONES_PLACED, GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                biomes, placed);
        add(context, VERDANT_MOOR_FLOOR, AscBiomes.VERDANT_MOOR,
                AscPlacedFeatures.VERDANT_MOOR_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);
        add(context, GOLDEN_STEPPE_STONES, AscBiomes.GOLDEN_STEPPE,
                AscPlacedFeatures.GOLDEN_STEPPE_STONES_PLACED, GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                biomes, placed);
        add(context, GOLDEN_STEPPE_FLOOR, AscBiomes.GOLDEN_STEPPE,
                AscPlacedFeatures.GOLDEN_STEPPE_FLOOR_SCATTER_PLACED, GenerationStep.Decoration.VEGETAL_DECORATION,
                biomes, placed);
    }

    private static void add(
            BootstrapContext<BiomeModifier> context,
            ResourceKey<BiomeModifier> modifierKey,
            ResourceKey<Biome> biomeKey,
            ResourceKey<PlacedFeature> featureKey,
            GenerationStep.Decoration step,
            net.minecraft.core.HolderGetter<Biome> biomes,
            net.minecraft.core.HolderGetter<PlacedFeature> placed
    ) {
        context.register(modifierKey, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(biomeKey)),
                HolderSet.direct(placed.getOrThrow(featureKey)),
                step
        ));
    }

    private static ResourceKey<BiomeModifier> key(String path) {
        return ResourceKey.create(
                NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path)
        );
    }
}
