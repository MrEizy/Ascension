package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PurityStatScaling(double threshold, double excessFlatRate, double excessMultiplierRate) {
    public static final Codec<PurityStatScaling> CODEC = RecordCodecBuilder.<PurityStatScaling>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("flat_threshold").forGetter(PurityStatScaling::threshold),
            Codec.DOUBLE.fieldOf("excess_flat_rate").forGetter(PurityStatScaling::excessFlatRate),
            Codec.DOUBLE.fieldOf("excess_multiplier_rate").forGetter(PurityStatScaling::excessMultiplierRate)
    ).apply(instance, PurityStatScaling::new)).validate(value ->
            nonNegativeFinite(value.threshold) && nonNegativeFinite(value.excessFlatRate)
                    && nonNegativeFinite(value.excessMultiplierRate)
                    ? DataResult.success(value) : DataResult.error(() -> "Purity scaling values must be finite and non-negative"));

    public double flat(double total) { return finite(Math.min(total, threshold) + Math.max(0, total - threshold) * excessFlatRate); }
    public double multiplier(double total) { return finite(Math.max(0, total - threshold) * excessMultiplierRate); }

    public static boolean nonNegativeFinite(double value) {
        return Double.isFinite(value) && value >= 0;
    }

    private static double finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Stat scaling produced a non-finite value");
        return value;
    }
}
