package net.zic.ascension.worldgen.debug;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.zic.ascension.AscensionCraft;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldgenDebugSampler {

    private static final String PREFIX = "terrain/";

    private static final String[] SAMPLE_FIELDS = {
            "continents",
            "land_mask",
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

    public static TerrainSample sample(RegistryAccess access, int x, int z) {
        var registry = access.lookupOrThrow(Registries.DENSITY_FUNCTION);
        DensityFunction.FunctionContext context = new SampleContext(x, 64, z);
        Map<String, Double> values = new LinkedHashMap<>();

        for (String field : SAMPLE_FIELDS) {
            ResourceKey<DensityFunction> key = ResourceKey.create(
                    Registries.DENSITY_FUNCTION,
                    AscensionCraft.prefix(PREFIX + field)
            );
            DensityFunction function = registry.getOrThrow(key).value();
            values.put(field, function.compute(context));
        }

        return new TerrainSample(x, z, values);
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
            if (value("hero_score") >= 0.40) {
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
