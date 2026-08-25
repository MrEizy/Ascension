package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ErosionDensityFunction(
        DensityFunction input,
        int sampleDistance,
        double strength,
        double curvatureScale,
        double slopeScale
) implements DensityFunction {

    public static final MapCodec<ErosionDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(ErosionDensityFunction::input),
                    Codec.INT.fieldOf("sample_distance").forGetter(ErosionDensityFunction::sampleDistance),
                    Codec.DOUBLE.fieldOf("strength").forGetter(ErosionDensityFunction::strength),
                    Codec.DOUBLE.fieldOf("curvature_scale").forGetter(ErosionDensityFunction::curvatureScale),
                    Codec.DOUBLE.fieldOf("slope_scale").forGetter(ErosionDensityFunction::slopeScale)
            ).apply(instance, ErosionDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<ErosionDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int distance = Math.max(1, sampleDistance);
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        double center = input.compute(context);
        double west = input.compute(new SampleContext(x - distance, y, z));
        double east = input.compute(new SampleContext(x + distance, y, z));
        double north = input.compute(new SampleContext(x, y, z - distance));
        double south = input.compute(new SampleContext(x, y, z + distance));

        double neighbourAverage = (west + east + north + south) * 0.25;
        double curvature = center - neighbourAverage;

        double denominator = 2.0 * distance;
        double dx = (east - west) / denominator;
        double dz = (south - north) / denominator;
        double slope = Math.sqrt(dx * dx + dz * dz);

        double slopeWeight = clamp01(slope * Math.max(0.0, slopeScale));
        double relief = Math.tanh(curvature * curvatureScale)
                * Math.abs(strength)
                * slopeWeight;

        return center + relief * Math.signum(strength);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new ErosionDensityFunction(
                input.mapAll(visitor),
                sampleDistance,
                strength,
                curvatureScale,
                slopeScale
        );
    }

    @Override
    public double minValue() {
        return input.minValue() - Math.abs(strength);
    }

    @Override
    public double maxValue() {
        return input.maxValue() + Math.abs(strength);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record SampleContext(int blockX, int blockY, int blockZ) implements FunctionContext {
    }
}
