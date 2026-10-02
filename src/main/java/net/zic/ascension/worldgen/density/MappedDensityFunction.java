package net.zic.ascension.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Locale;

public record MappedDensityFunction(
        DensityFunction input,
        Operation operation
) implements DensityFunction {

    public enum Operation {
        ABS,
        SQUEEZE;

        private static final Codec<Operation> CODEC = Codec.STRING.xmap(
                value -> Operation.valueOf(value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT)
        );

        double apply(double value) {
            return switch (this) {
                case ABS -> Math.abs(value);
                case SQUEEZE -> {
                    double clamped = Math.max(-1.0D, Math.min(1.0D, value));
                    yield clamped / 2.0D - clamped * clamped * clamped / 24.0D;
                }
            };
        }
    }

    public static final MapCodec<MappedDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(MappedDensityFunction::input),
                    Operation.CODEC.fieldOf("operation").forGetter(MappedDensityFunction::operation)
            ).apply(instance, MappedDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<MappedDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return operation.apply(input.compute(context));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        input.fillArray(values, contextProvider);
        for (int i = 0; i < values.length; i++) {
            values[i] = operation.apply(values[i]);
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new MappedDensityFunction(input.mapAll(visitor), operation);
    }

    @Override
    public double minValue() {
        double min = input.minValue();
        double max = input.maxValue();

        return switch (operation) {
            case ABS -> min <= 0.0D && max >= 0.0D
                    ? 0.0D
                    : Math.min(Math.abs(min), Math.abs(max));
            case SQUEEZE -> operation.apply(min);
        };
    }

    @Override
    public double maxValue() {
        double min = input.minValue();
        double max = input.maxValue();

        return switch (operation) {
            case ABS -> Math.max(Math.abs(min), Math.abs(max));
            case SQUEEZE -> operation.apply(max);
        };
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
