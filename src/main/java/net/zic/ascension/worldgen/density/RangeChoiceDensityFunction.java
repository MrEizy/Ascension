package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record RangeChoiceDensityFunction(
        DensityFunction input,
        double minInclusive,
        double maxExclusive,
        DensityFunction whenInRange,
        DensityFunction whenOutOfRange
) implements DensityFunction {

    public static final MapCodec<RangeChoiceDensityFunction> DATA_CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(RangeChoiceDensityFunction::input),
                            Codec.DOUBLE.fieldOf("min_inclusive").forGetter(RangeChoiceDensityFunction::minInclusive),
                            Codec.DOUBLE.fieldOf("max_exclusive").forGetter(RangeChoiceDensityFunction::maxExclusive),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(RangeChoiceDensityFunction::whenInRange),
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(RangeChoiceDensityFunction::whenOutOfRange)
                    ).apply(instance, RangeChoiceDensityFunction::new)
            );

    public static final KeyDispatchDataCodec<RangeChoiceDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    public RangeChoiceDensityFunction {
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException(
                    "Range choice minimum must be less than maximum"
            );
        }
    }

    @Override
    public double compute(FunctionContext context) {
        double value = input.compute(context);
        return value >= minInclusive && value < maxExclusive ? whenInRange.compute(context) : whenOutOfRange.compute(context);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new RangeChoiceDensityFunction(
                input.mapAll(visitor),
                minInclusive,
                maxExclusive,
                whenInRange.mapAll(visitor),
                whenOutOfRange.mapAll(visitor)
        );
    }

    @Override
    public double minValue() {
        return Math.min(whenInRange.minValue(), whenOutOfRange.minValue());
    }

    @Override
    public double maxValue() {
        return Math.max(whenInRange.maxValue(), whenOutOfRange.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}