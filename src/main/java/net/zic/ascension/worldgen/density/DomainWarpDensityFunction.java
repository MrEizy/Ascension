package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record DomainWarpDensityFunction(
        DensityFunction input,
        DensityFunction shiftX,
        DensityFunction shiftZ,
        double amplitude
) implements DensityFunction {

    public static final MapCodec<DomainWarpDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DomainWarpDensityFunction::input),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DomainWarpDensityFunction::shiftX),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DomainWarpDensityFunction::shiftZ),
                    Codec.DOUBLE.fieldOf("amplitude").forGetter(DomainWarpDensityFunction::amplitude)
            ).apply(instance, DomainWarpDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<DomainWarpDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX() + (int) Math.round(shiftX.compute(context) * amplitude);
        int z = context.blockZ() + (int) Math.round(shiftZ.compute(context) * amplitude);
        return input.compute(new SampleContext(x, context.blockY(), z));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new DomainWarpDensityFunction(
                input.mapAll(visitor),
                shiftX.mapAll(visitor),
                shiftZ.mapAll(visitor),
                amplitude
        );
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private record SampleContext(int blockX, int blockY, int blockZ) implements FunctionContext {
    }
}
