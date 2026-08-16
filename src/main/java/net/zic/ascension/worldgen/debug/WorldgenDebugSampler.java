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
            "mountain_target_raw_y",
            "mountain_target_uncarved_y",
            "mountain_target_y",
            "mountain_eroded_y",
            "surface_target_y"
    };

    private static final String[] BIOME_FIELDS = {
            "biome_temperature",
            "biome_humidity",
            "biome_continentalness",
            "biome_erosion",
            "biome_depth",
            "biome_weirdness",
            "biome_surface_depth",
            "biome_surface_proximity"
    };

    private static final String[] SCAN_FIELDS = {
            "land_mask",
            "orogeny",
            "mountain_province",
            "mountain_axis_abs",
            "foothill_mask",
            "core_mask",
            "massif_noise",
            "hero_gate",
            "hero_score",
            "plateau_mask",
            "rolling_mask",
            "mountain_target_raw_y",
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
        Map<String, DensityFunction> functions = mapFields(densityRegistry, mapper, SAMPLE_FIELDS);
        Map<String, DensityFunction> scanFunctions = selectFields(functions, SCAN_FIELDS);
        Map<String, DensityFunction> biomeFunctions = mapFields(densityRegistry, mapper, BIOME_FIELDS);

        return new Sampler(functions, scanFunctions, biomeFunctions, noiseGenerator, randomState);
    }

    private static Map<String, DensityFunction> mapFields(
            net.minecraft.core.HolderLookup.RegistryLookup<DensityFunction> densityRegistry,
            RuntimeDensityFunctionMapper mapper,
            String[] fields
    ) {
        Map<String, DensityFunction> functions = new LinkedHashMap<>();
        for (String field : fields) {
            ResourceKey<DensityFunction> key = ResourceKey.create(
                    Registries.DENSITY_FUNCTION,
                    AscensionCraft.prefix(PREFIX + field)
            );
            DensityFunction runtimeFunction = densityRegistry.getOrThrow(key).value().mapAll(mapper);
            functions.put(field, runtimeFunction);
        }
        return Map.copyOf(functions);
    }

    private static Map<String, DensityFunction> selectFields(
            Map<String, DensityFunction> functions,
            String[] fields
    ) {
        Map<String, DensityFunction> selected = new LinkedHashMap<>();
        for (String field : fields) {
            DensityFunction function = functions.get(field);
            if (function != null) {
                selected.put(field, function);
            }
        }
        return Map.copyOf(selected);
    }

    public static final class Sampler {
        private final Map<String, DensityFunction> functions;
        private final Map<String, DensityFunction> scanFunctions;
        private final Map<String, DensityFunction> biomeFunctions;
        private final NoiseBasedChunkGenerator noiseGenerator;
        private final RandomState randomState;

        private Sampler(
                Map<String, DensityFunction> functions,
                Map<String, DensityFunction> scanFunctions,
                Map<String, DensityFunction> biomeFunctions,
                NoiseBasedChunkGenerator noiseGenerator,
                RandomState randomState
        ) {
            this.functions = functions;
            this.scanFunctions = scanFunctions;
            this.biomeFunctions = biomeFunctions;
            this.noiseGenerator = noiseGenerator;
            this.randomState = randomState;
        }

        public TerrainSample sample(int x, int z) {
            return sampleFields(functions, x, 64, z);
        }

        public TerrainSample sampleForScan(int x, int z) {
            return sampleFields(scanFunctions, x, 64, z);
        }

        public BiomeClimateSample sampleBiomeClimate(int x, int y, int z) {
            TerrainSample values = sampleFields(biomeFunctions, x, y, z);
            return new BiomeClimateSample(values.values());
        }

        private static TerrainSample sampleFields(Map<String, DensityFunction> fields, int x, int y, int z) {
            DensityFunction.FunctionContext context = new SampleContext(x, y, z);
            Map<String, Double> values = new LinkedHashMap<>();

            for (Map.Entry<String, DensityFunction> entry : fields.entrySet()) {
                values.put(entry.getKey(), entry.getValue().compute(context));
            }

            return new TerrainSample(x, z, Map.copyOf(values));
        }

        public GeneratorColumnSample sampleGeneratorColumn(int x, int z, double ascensionTargetY, int actualSurfaceY) {
            int minY = noiseGenerator.getMinY();
            int maxYExclusive = minY + noiseGenerator.getGenDepth();
            int generatorSurfaceY = Integer.MIN_VALUE;

            final int stride = 8;
            int positiveBandY = Integer.MIN_VALUE;
            for (int y = maxYExclusive - 1; y >= minY; y -= stride) {
                double density = generatorDensity(x, y, z);
                if (!Double.isNaN(density) && density > 0.0) {
                    positiveBandY = y;
                    break;
                }
            }
            if (positiveBandY != Integer.MIN_VALUE) {
                int refineTop = Math.min(maxYExclusive - 1, positiveBandY + stride - 1);
                for (int y = refineTop; y >= positiveBandY; y--) {
                    double density = generatorDensity(x, y, z);
                    if (!Double.isNaN(density) && density > 0.0) {
                        generatorSurfaceY = y + 1;
                        break;
                    }
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


    public record BiomeClimateSample(Map<String, Double> values) {
        public double value(String id) {
            return values.getOrDefault(id, 0.0);
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
            if (value("hero_score") >= 0.50) {
                return "hero massif";
            }
            if (value("core_mask") >= 0.50) {
                return "mountain core";
            }
            if (value("foothill_mask") >= 0.34) {
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
