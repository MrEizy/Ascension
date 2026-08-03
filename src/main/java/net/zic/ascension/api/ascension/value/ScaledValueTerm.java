package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScaledValueTerm(
        ScaledValueSource source,
        ScaledValueOperation operation,
        double power,
        double scale,
        double offset
) {
    public static final MapCodec<ScaledValueTerm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValueSource.CODEC.fieldOf("source").forGetter(ScaledValueTerm::source),
            ScaledValueOperation.CODEC.optionalFieldOf("operation", ScaledValueOperation.ADD)
                    .forGetter(ScaledValueTerm::operation),
            Codec.DOUBLE.optionalFieldOf("power", 1.0D).forGetter(ScaledValueTerm::power),
            Codec.DOUBLE.optionalFieldOf("scale", 1.0D).forGetter(ScaledValueTerm::scale),
            Codec.DOUBLE.optionalFieldOf("offset", 0.0D).forGetter(ScaledValueTerm::offset)
    ).apply(instance, ScaledValueTerm::new));

    public double apply(double current, ScaledValueContext context) {
        double sourceValue = source.resolve(context);
        if (!Double.isFinite(sourceValue)) {
            sourceValue = 0.0D;
        }
        double poweredValue = Math.pow(sourceValue, power);
        double value = poweredValue * scale + offset;
        if (!Double.isFinite(value)) {
            return current;
        }
        return operation.apply(current, value);
    }
}
