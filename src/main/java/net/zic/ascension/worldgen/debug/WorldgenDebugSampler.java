package net.zic.ascension.worldgen.debug;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.zic.ascension.AscensionCraft;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime sampler for Ascension terrain density functions and the active noise generator.
 */
public final class WorldgenDebugSampler {

    private static final String PREFIX = "terrain/";

    private static final String[] SAMPLE_FIELDS = {
            "continents",
            "land_mask",
            "mountain_land_gate",
            "orogeny",
            "mountain_province",
            "mountain_axis_abs",
            "foothill_mask",
            "core_mask",
            "ridge_score",
            "massif_noise",
            "hero_gate",
            "hero_score",
            "valley_score",
            "plateau_mask",
            "rolling_mask",
            "lowland_target_y",
            "plateau_target_y",
            "mountain_target_uncarved_y",
            "mountain_target_y",
            "mountain_eroded_y",
            "surface_target_y"
    };

    private WorldgenDebugSampler() {
    }

    public static Sampler create(ServerLevel level) {
        var chunkGenerator = level.getChunkSource().getGenerator();
        if (!(chunkGenerator instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            throw new IllegalStateException("Ascension worldgen debugging requires a NoiseBasedChunkGenerator");
        }

        var settings = noiseGenerator.generatorSettings().value();
        var noiseRegistry = level.registryAccess().lookupOrThrow(Registries.NOISE);
        RandomState randomState = RandomState.create(settings, noiseRegistry, level.getSeed());
        RuntimeDensityFunctionMapper mapper = new RuntimeDensityFunctionMapper(randomState);

        var densityRegistry = level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION);
        Map<String, DensityFunction> functions = new LinkedHashMap<>();

        for (String field : SAMPLE_FIELDS) {
            ResourceKey<DensityFunction> key = ResourceKey.create(
                    Registries.DENSITY_FUNCTION,
                    AscensionCraft.prefix(PREFIX + field)
            );
            DensityFunction runtimeFunction = densityRegistry.getOrThrow(key).value().mapAll(mapper);
            functions.put(field, runtimeFunction);
        }

        return new Sampler(functions, noiseGenerator, randomState);
    }

    public static final class Sampler {
        private final Map<String, DensityFunction> functions;
        private final NoiseBasedChunkGenerator noiseGenerator;
        private final RandomState randomState;

        private Sampler(
                Map<String, DensityFunction> functions,
                NoiseBasedChunkGenerator noiseGenerator,
                RandomState randomState
        ) {
            this.functions = Map.copyOf(functions);
            this.noiseGenerator = noiseGenerator;
            this.randomState = randomState;
        }

        public TerrainSample sample(int x, int z) {
            DensityFunction.FunctionContext context = new SampleContext(x, 64, z);
            Map<String, Double> values = new LinkedHashMap<>();

            for (Map.Entry<String, DensityFunction> entry : functions.entrySet()) {
                values.put(entry.getKey(), entry.getValue().compute(context));
            }

            return new TerrainSample(x, z, values);
        }

        public GeneratorColumnSample sampleGeneratorColumn(int x, int z, double ascensionTargetY, int actualSurfaceY) {
            int minY = noiseGenerator.getMinY();
            int maxYExclusive = minY + noiseGenerator.getGenDepth();
            int generatorSurfaceY = Integer.MIN_VALUE;

            for (int y = maxYExclusive - 1; y >= minY; y--) {
                double density = generatorDensity(x, y, z);
                if (!Double.isNaN(density) && density > 0.0) {
                    generatorSurfaceY = y + 1;
                    break;
                }
            }

            int targetProbeY = clampY((int) Math.round(ascensionTargetY), minY, maxYExclusive - 1);
            int actualProbeY = clampY(actualSurfaceY - 1, minY, maxYExclusive - 1);

            double densityAtTarget = generatorDensity(x, targetProbeY, z);
            double densityAtActual = generatorDensity(x, actualProbeY, z);

            return new GeneratorColumnSample(
                    generatorSurfaceY,
                    minY,
                    maxYExclusive,
                    targetProbeY,
                    densityAtTarget,
                    actualProbeY,
                    densityAtActual
            );
        }

        private double generatorDensity(int x, int y, int z) {
            return noiseGenerator.getInterpolatedNoiseValue(
                    randomState,
                    new SampleContext(x, y, z)
            );
        }

        private static int clampY(int y, int minY, int maxY) {
            return Math.max(minY, Math.min(maxY, y));
        }
    }

    public record GeneratorColumnSample(
            int surfaceY,
            int minY,
            int maxYExclusive,
            int targetProbeY,
            double densityAtTarget,
            int actualProbeY,
            double densityAtActual
    ) {
        public boolean foundSurface() {
            return surfaceY != Integer.MIN_VALUE;
        }
    }

    public record TerrainSample(int x, int z, Map<String, Double> values) {
        public double value(String id) {
            return values.getOrDefault(id, 0.0);
        }

        public double predictedSurfaceY() {
            return value("surface_target_y");
        }

        public String regionName() {
            if (value("land_mask") < 0.20) {
                return "ocean/coast";
            }
            if (value("hero_score") >= 0.38) {
                return "hero massif";
            }
            if (value("core_mask") >= 0.45) {
                return "mountain core";
            }
            if (value("foothill_mask") >= 0.38) {
                return "foothills";
            }
            if (value("plateau_mask") >= 0.40) {
                return "plateau";
            }
            if (value("rolling_mask") >= 0.30) {
                return "rolling upland";
            }
            return "lowland";
        }
    }

    private record SampleContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
    }
}
