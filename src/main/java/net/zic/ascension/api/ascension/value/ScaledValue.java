package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record ScaledValue(
        double base,
        List<ScaledValueTerm> terms,
        Optional<Double> minimum,
        Optional<Double> maximum
) {
    public static final MapCodec<ScaledValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("base", 0.0D).forGetter(ScaledValue::base),
            ScaledValueTerm.CODEC.codec().listOf().optionalFieldOf("terms", List.of())
                    .forGetter(ScaledValue::terms),
            Codec.DOUBLE.optionalFieldOf("minimum").forGetter(ScaledValue::minimum),
            Codec.DOUBLE.optionalFieldOf("maximum").forGetter(ScaledValue::maximum)
    ).apply(instance, ScaledValue::new));

    public ScaledValue {
        terms = terms == null ? List.of() : List.copyOf(terms);
        minimum = minimum == null ? Optional.empty() : minimum;
        maximum = maximum == null ? Optional.empty() : maximum;
    }

    public static ScaledValue constant(double value) {
        return new ScaledValue(value, List.of(), Optional.empty(), Optional.empty());
    }

    public double resolve(ScaledValueContext context) {
        double value = base;
        for (ScaledValueTerm term : terms) {
            value = term.apply(value, context);
        }

        if (!Double.isFinite(value)) {
            value = 0.0D;
        }
        double lowerBound = minimum.orElse(Double.NEGATIVE_INFINITY);
        double upperBound = maximum.orElse(Double.POSITIVE_INFINITY);
        if (lowerBound > upperBound) {
            upperBound = lowerBound;
        }
        return Math.max(lowerBound, Math.min(upperBound, value));
    }
}
