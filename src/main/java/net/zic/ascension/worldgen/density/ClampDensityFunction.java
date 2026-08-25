package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ClampDensityFunction(
        DensityFunction input,
        double minValue,
        double maxValue
) implements DensityFunction {

    public static final MapCodec<ClampDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(ClampDensityFunction::input),
                    Codec.DOUBLE.fieldOf("min").forGetter(ClampDensityFunction::minValue),
                    Codec.DOUBLE.fieldOf("max").forGetter(ClampDensityFunction::maxValue)
            ).apply(instance, ClampDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<ClampDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    public ClampDensityFunction {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("Clamp minimum cannot be greater than maximum");
        }
    }

    @Override
    public double compute(FunctionContext context) {
        return clamp(input.compute(context));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        input.fillArray(values, contextProvider);
        for (int i = 0; i < values.length; i++) {
            values[i] = clamp(values[i]);
        }
    }

    private double clamp(double value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new ClampDensityFunction(input.mapAll(visitor), minValue, maxValue);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
