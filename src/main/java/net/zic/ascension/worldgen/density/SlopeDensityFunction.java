package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record SlopeDensityFunction(
        DensityFunction input,
        int sampleDistance,
        double scale,
        double max
) implements DensityFunction {

    public static final MapCodec<SlopeDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(SlopeDensityFunction::input),
                    Codec.INT.fieldOf("sample_distance").forGetter(SlopeDensityFunction::sampleDistance),
                    Codec.DOUBLE.fieldOf("scale").forGetter(SlopeDensityFunction::scale),
                    Codec.DOUBLE.fieldOf("max").forGetter(SlopeDensityFunction::max)
            ).apply(instance, SlopeDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<SlopeDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int distance = Math.max(1, sampleDistance);
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        double west = input.compute(new SampleContext(x - distance, y, z));
        double east = input.compute(new SampleContext(x + distance, y, z));
        double north = input.compute(new SampleContext(x, y, z - distance));
        double south = input.compute(new SampleContext(x, y, z + distance));

        double denominator = 2.0 * distance;
        double dx = (east - west) / denominator;
        double dz = (south - north) / denominator;
        double slope = Math.sqrt(dx * dx + dz * dz) * Math.max(0.0, scale);

        return Math.min(Math.max(0.0, max), slope);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new SlopeDensityFunction(
                input.mapAll(visitor),
                sampleDistance,
                scale,
                max
        );
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return Math.max(0.0, max);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private record SampleContext(int blockX, int blockY, int blockZ) implements FunctionContext {
    }
}
