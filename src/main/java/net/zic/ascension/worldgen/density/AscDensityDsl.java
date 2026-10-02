package net.zic.ascension.worldgen.density;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;

final class AscDensityDsl {
    private final HolderGetter<DensityFunction> densityFunctions;
    private final HolderGetter<NormalNoise.NoiseParameters> noises;

    AscDensityDsl(BootstrapContext<DensityFunction> context) {
        this.densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);
        this.noises = context.lookup(Registries.NOISE);
    }

    DensityFunction ref(ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }

    DensityFunction vanillaDensity(String path) {
        return ref(ResourceKey.create(
                Registries.DENSITY_FUNCTION,
                Identifier.fromNamespaceAndPath("minecraft", path)
        ));
    }

    ResourceKey<NormalNoise.NoiseParameters> vanillaNoiseKey(String path) {
        return ResourceKey.create(
                Registries.NOISE,
                Identifier.fromNamespaceAndPath("minecraft", path)
        );
    }

    DensityFunction constant(double value) {
        return DensityFunctions.constant(value);
    }

    DensityFunction add(DensityFunction a, DensityFunction b) {
        return DensityFunctions.add(a, b);
    }

    DensityFunction mul(DensityFunction a, DensityFunction b) {
        return DensityFunctions.mul(a, b);
    }

    DensityFunction min(DensityFunction a, DensityFunction b) {
        return DensityFunctions.min(a, b);
    }

    DensityFunction max(DensityFunction a, DensityFunction b) {
        return DensityFunctions.max(a, b);
    }

    DensityFunction clamp(DensityFunction input, double min, double max) {
        return new ClampDensityFunction(input, min, max);
    }

    DensityFunction abs(DensityFunction input) {
        return new MappedDensityFunction(input, MappedDensityFunction.Operation.ABS);
    }

    DensityFunction flatCache(DensityFunction input) {
        return DensityFunctions.flatCache(input);
    }

    DensityFunction interpolated(DensityFunction input) {
        return DensityFunctions.interpolated(input);
    }

    DensityFunction blendDensity(DensityFunction input) {
        return DensityFunctions.blendDensity(input);
    }

    DensityFunction squeeze(DensityFunction input) {
        return new MappedDensityFunction(input, MappedDensityFunction.Operation.SQUEEZE);
    }

    DensityFunction yGradient(int fromY, int toY, double fromValue, double toValue) {
        return DensityFunctions.yClampedGradient(fromY, toY, fromValue, toValue);
    }

    DensityFunction noise(ResourceKey<NormalNoise.NoiseParameters> key, double xzScale, double yScale) {
        return DensityFunctions.noise(noises.getOrThrow(key), xzScale, yScale);
    }

    DensityFunction shiftedNoise2d(
            DensityFunction shiftX,
            DensityFunction shiftZ,
            double xzScale,
            ResourceKey<NormalNoise.NoiseParameters> key
    ) {
        return DensityFunctions.shiftedNoise2d(shiftX, shiftZ, xzScale, noises.getOrThrow(key));
    }

    DensityFunction spline(DensityFunction coordinate, List<HermiteSplineDensityFunction.Point> points) {
        return new HermiteSplineDensityFunction(coordinate, points);
    }
}
