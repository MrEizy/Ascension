package net.zic.ascension.worldgen.dimension;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.biome.AscOverworldBiomeParameters;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AscWorldPresets {

    public static final ResourceKey<WorldPreset> ASCENSION = ResourceKey.create(
            Registries.WORLD_PRESET,
            AscensionCraft.prefix("ascension")
    );

    private static final ResourceKey<DimensionType> NETHER_DIMENSION_TYPE =
            ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    Identifier.fromNamespaceAndPath("minecraft", "the_nether")
            );

    private static final ResourceKey<DimensionType> END_DIMENSION_TYPE =
            ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    Identifier.fromNamespaceAndPath("minecraft", "the_end")
            );

    private static final ResourceKey<NoiseGeneratorSettings> NETHER_NOISE_SETTINGS =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.fromNamespaceAndPath("minecraft", "nether")
            );

    private static final ResourceKey<NoiseGeneratorSettings> END_NOISE_SETTINGS =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.fromNamespaceAndPath("minecraft", "end")
            );

    private static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER_BIOME_PARAMETERS =
            ResourceKey.create(
                    Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
                    Identifier.fromNamespaceAndPath("minecraft", "nether")
            );

    private AscWorldPresets() {
    }

    public static void bootstrap(BootstrapContext<WorldPreset> context) {
        HolderGetter<DimensionType> dimensionTypes =
                context.lookup(Registries.DIMENSION_TYPE);

        HolderGetter<NoiseGeneratorSettings> noiseSettings =
                context.lookup(Registries.NOISE_SETTINGS);

        HolderGetter<Biome> biomes =
                context.lookup(Registries.BIOME);

        HolderGetter<MultiNoiseBiomeSourceParameterList> biomeParameterLists =
                context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);


        Holder<DimensionType> ascensionDimensionType = dimensionTypes.getOrThrow(AscDimensionTypes.OVERWORLD);
        Holder<NoiseGeneratorSettings> ascensionNoiseSettings = noiseSettings.getOrThrow(AscNoiseSettings.OVERWORLD);

        MultiNoiseBiomeSource ascensionBiomeSource =
                MultiNoiseBiomeSource.createFromList(
                        AscOverworldBiomeParameters.parameterList(biomes)
                );

        LevelStem ascensionOverworld = new LevelStem(
                ascensionDimensionType,
                new NoiseBasedChunkGenerator(
                        ascensionBiomeSource,
                        ascensionNoiseSettings
                )
        );


        Holder<DimensionType> netherDimensionType = dimensionTypes.getOrThrow(NETHER_DIMENSION_TYPE);
        Holder<NoiseGeneratorSettings> netherNoiseSettings = noiseSettings.getOrThrow(NETHER_NOISE_SETTINGS);

        MultiNoiseBiomeSource netherBiomeSource =
                MultiNoiseBiomeSource.createFromPreset(
                        biomeParameterLists.getOrThrow(NETHER_BIOME_PARAMETERS)
                );

        LevelStem nether = new LevelStem(
                netherDimensionType,
                new NoiseBasedChunkGenerator(
                        netherBiomeSource,
                        netherNoiseSettings
                )
        );

        Holder<DimensionType> endDimensionType = dimensionTypes.getOrThrow(END_DIMENSION_TYPE);
        Holder<NoiseGeneratorSettings> endNoiseSettings = noiseSettings.getOrThrow(END_NOISE_SETTINGS);

        LevelStem end = new LevelStem(
                endDimensionType,
                new NoiseBasedChunkGenerator(
                        TheEndBiomeSource.create(biomes),
                        endNoiseSettings
                )
        );

        Map<ResourceKey<LevelStem>, LevelStem> dimensions =
                new LinkedHashMap<>();

        dimensions.put(LevelStem.OVERWORLD, ascensionOverworld);
        dimensions.put(LevelStem.NETHER, nether);
        dimensions.put(LevelStem.END, end);

        context.register(
                ASCENSION,
                new WorldPreset(Map.copyOf(dimensions))
        );
    }
}