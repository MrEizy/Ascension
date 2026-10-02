package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record TerraceDensityFunction(
        DensityFunction input,
        double stepSize,
        double strength,
        double transition
) implements DensityFunction {

    public static final MapCodec<TerraceDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(TerraceDensityFunction::input),
                    Codec.DOUBLE.fieldOf("step_size").forGetter(TerraceDensityFunction::stepSize),
                    Codec.DOUBLE.fieldOf("strength").forGetter(TerraceDensityFunction::strength),
                    Codec.DOUBLE.fieldOf("transition").forGetter(TerraceDensityFunction::transition)
            ).apply(instance, TerraceDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<TerraceDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        double value = input.compute(context);
        double step = Math.max(1.0E-4, Math.abs(stepSize));
        double mix = clamp01(strength);
        double width = Math.max(1.0E-4, Math.min(0.49, Math.abs(transition)));

        double scaled = value / step;
        double level = Math.floor(scaled);
        double fraction = scaled - level;

        double lowerEdge = 0.5 - width;
        double upperEdge = 0.5 + width;
        double shelfFraction;

        if (fraction <= lowerEdge) {
            shelfFraction = 0.0;
        } else if (fraction >= upperEdge) {
            shelfFraction = 1.0;
        } else {
            double t = (fraction - lowerEdge) / (upperEdge - lowerEdge);
            shelfFraction = t * t * (3.0 - 2.0 * t);
        }

        double terraced = (level + shelfFraction) * step;
        return value + (terraced - value) * mix;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        input.fillArray(values, contextProvider);
        for (int i = 0; i < values.length; i++) {
            values[i] = transform(values[i]);
        }
    }

    private double transform(double value) {
        double step = Math.max(1.0E-4, Math.abs(stepSize));
        double mix = clamp01(strength);
        double width = Math.max(1.0E-4, Math.min(0.49, Math.abs(transition)));

        double scaled = value / step;
        double level = Math.floor(scaled);
        double fraction = scaled - level;
        double lowerEdge = 0.5 - width;
        double upperEdge = 0.5 + width;
        double shelfFraction;

        if (fraction <= lowerEdge) {
            shelfFraction = 0.0;
        } else if (fraction >= upperEdge) {
            shelfFraction = 1.0;
        } else {
            double t = (fraction - lowerEdge) / (upperEdge - lowerEdge);
            shelfFraction = t * t * (3.0 - 2.0 * t);
        }

        double terraced = (level + shelfFraction) * step;
        return value + (terraced - value) * mix;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new TerraceDensityFunction(
                input.mapAll(visitor),
                stepSize,
                strength,
                transition
        );
    }

    @Override
    public double minValue() {
        return input.minValue() - Math.abs(stepSize);
    }

    @Override
    public double maxValue() {
        return input.maxValue() + Math.abs(stepSize);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
