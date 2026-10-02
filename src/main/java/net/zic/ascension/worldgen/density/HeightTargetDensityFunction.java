package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record HeightTargetDensityFunction(
        DensityFunction targetY,
        double baselineY,
        double blocksPerDensity
) implements DensityFunction {

    public static final MapCodec<HeightTargetDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("target_y").forGetter(HeightTargetDensityFunction::targetY),
                    Codec.DOUBLE.optionalFieldOf("baseline_y", 64.0).forGetter(HeightTargetDensityFunction::baselineY),
                    Codec.DOUBLE.optionalFieldOf("blocks_per_density", 64.0).forGetter(HeightTargetDensityFunction::blocksPerDensity)
            ).apply(instance, HeightTargetDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<HeightTargetDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return convert(targetY.compute(context));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        targetY.fillArray(values, contextProvider);
        for (int i = 0; i < values.length; i++) {
            values[i] = convert(values[i]);
        }
    }

    private double convert(double targetHeight) {
        double scale = Math.abs(blocksPerDensity) < 1.0E-6 ? 64.0 : blocksPerDensity;
        return (targetHeight - baselineY) / scale;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new HeightTargetDensityFunction(targetY.mapAll(visitor), baselineY, blocksPerDensity);
    }

    @Override
    public double minValue() {
        double a = convert(targetY.minValue());
        double b = convert(targetY.maxValue());
        return Math.min(a, b);
    }

    @Override
    public double maxValue() {
        double a = convert(targetY.minValue());
        double b = convert(targetY.maxValue());
        return Math.max(a, b);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
