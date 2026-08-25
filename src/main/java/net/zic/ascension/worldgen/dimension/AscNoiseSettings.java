package net.zic.ascension.worldgen.dimension;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.density.AscDensityFunctions;
import net.zic.ascension.worldgen.density.MappedDensityFunction;
import net.zic.ascension.worldgen.density.RangeChoiceDensityFunction;
import net.zic.ascension.worldgen.surface.AscSurfaceRules;

public final class AscNoiseSettings {

    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    AscensionCraft.prefix("overworld")
            );

    private static final ResourceKey<NoiseGeneratorSettings>
            VANILLA_OVERWORLD = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            Identifier.fromNamespaceAndPath(
                    "minecraft",
                    "overworld"
            )
    );

    private static final ResourceKey<DensityFunction>
            VANILLA_Y = ResourceKey.create(
            Registries.DENSITY_FUNCTION,
            Identifier.fromNamespaceAndPath(
                    "minecraft",
                    "y"
            )
    );

    private AscNoiseSettings() {
    }

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {

        HolderLookup.Provider vanillaRegistries = VanillaRegistries.createLookup();

        NoiseGeneratorSettings vanilla = vanillaRegistries
                .lookupOrThrow(Registries.NOISE_SETTINGS)
                .getOrThrow(VANILLA_OVERWORLD)
                .value();

        HolderGetter<DensityFunction> densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);

        HolderGetter<NormalNoise.NoiseParameters> noises = context.lookup(Registries.NOISE);

        DensityFunction initialDensity = holder(
                densityFunctions,
                AscDensityFunctions
                        .INITIAL_DENSITY_WITHOUT_JAGGEDNESS
        );

        DensityFunction preliminarySurface =
                DensityFunctions.findTopSurface(
                        DensityFunctions.add(
                                DensityFunctions.constant(
                                        -0.390625D
                                ),
                                initialDensity
                        ),
                        DensityFunctions.constant(416.0D),
                        -64,
                        8
                );

        DensityFunction barrier = noise(
                noises,
                "aquifer_barrier",
                1.0D,
                0.5D
        );

        DensityFunction floodedness = noise(
                noises,
                "aquifer_fluid_level_floodedness",
                1.0D,
                0.67D
        );

        DensityFunction spread = noise(
                noises,
                "aquifer_fluid_level_spread",
                1.0D,
                0.7142857142857143D
        );

        DensityFunction lava = noise(
                noises,
                "aquifer_lava",
                1.0D,
                1.0D
        );

        DensityFunction vanillaY = holder(
                densityFunctions,
                VANILLA_Y
        );

        DensityFunction zero =
                DensityFunctions.constant(0.0D);

        DensityFunction veinToggle =
                DensityFunctions.interpolated(
                        new RangeChoiceDensityFunction(
                                vanillaY,
                                -60.0D,
                                51.0D,
                                noise(
                                        noises,
                                        "ore_veininess",
                                        1.5D,
                                        1.5D
                                ),
                                zero
                        )
                );

        DensityFunction veinA =
                new MappedDensityFunction(
                        DensityFunctions.interpolated(
                                new RangeChoiceDensityFunction(
                                        vanillaY,
                                        -60.0D,
                                        51.0D,
                                        noise(
                                                noises,
                                                "ore_vein_a",
                                                4.0D,
                                                4.0D
                                        ),
                                        zero
                                )
                        ),
                        MappedDensityFunction.Operation.ABS
                );

        DensityFunction veinB =
                new MappedDensityFunction(
                        DensityFunctions.interpolated(
                                new RangeChoiceDensityFunction(
                                        vanillaY,
                                        -60.0D,
                                        51.0D,
                                        noise(
                                                noises,
                                                "ore_vein_b",
                                                4.0D,
                                                4.0D
                                        ),
                                        zero
                                )
                        ),
                        MappedDensityFunction.Operation.ABS
                );

        DensityFunction veinRidged =
                DensityFunctions.add(
                        DensityFunctions.constant(
                                -0.07999999821186066D
                        ),
                        DensityFunctions.max(
                                veinA,
                                veinB
                        )
                );

        DensityFunction veinGap = noise(
                noises,
                "ore_gap",
                1.0D,
                1.0D
        );

        NoiseRouter router = new NoiseRouter(
                barrier,
                floodedness,
                spread,
                lava,

                holder(densityFunctions, AscDensityFunctions.BIOME_TEMPERATURE),
                holder(densityFunctions, AscDensityFunctions.BIOME_HUMIDITY),
                holder(densityFunctions, AscDensityFunctions.BIOME_CONTINENTALNESS),
                holder(densityFunctions, AscDensityFunctions.BIOME_EROSION),
                holder(densityFunctions, AscDensityFunctions.BIOME_DEPTH),
                holder(densityFunctions, AscDensityFunctions.BIOME_WEIRDNESS),

                preliminarySurface,

                holder(densityFunctions, AscDensityFunctions.FINAL_DENSITY),

                veinToggle,
                veinRidged,
                veinGap
        );

        context.register(
                OVERWORLD,
                new NoiseGeneratorSettings(
                        NoiseSettings.create(-64, 480, 1, 2),

                        Blocks.STONE.defaultBlockState(),
                        Blocks.WATER.defaultBlockState(),

                        router,
                        AscSurfaceRules.overworld(vanilla.surfaceRule()),

                        vanilla.spawnTarget(),

                        63,

                        false,
                        true,
                        true,
                        false
                )
        );
    }

    private static DensityFunction holder(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(
                densityFunctions.getOrThrow(key)
        );
    }

    private static DensityFunction noise(HolderGetter<NormalNoise.NoiseParameters> noises, String path, double xzScale, double yScale) {
        ResourceKey<NormalNoise.NoiseParameters> key =
                ResourceKey.create(
                        Registries.NOISE,
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                path
                        )
                );

        return DensityFunctions.noise(noises.getOrThrow(key), xzScale, yScale);
    }
}