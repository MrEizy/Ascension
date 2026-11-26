package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.progression.StatScalingMath;

public record PurityStatScaling(double threshold, double excessFlatRate, double excessMultiplierRate) {
    public static final Codec<PurityStatScaling> CODEC = RecordCodecBuilder.<PurityStatScaling>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("flat_threshold").forGetter(PurityStatScaling::threshold),
            Codec.DOUBLE.fieldOf("excess_flat_rate").forGetter(PurityStatScaling::excessFlatRate),
            Codec.DOUBLE.fieldOf("excess_multiplier_rate").forGetter(PurityStatScaling::excessMultiplierRate)
    ).apply(instance, PurityStatScaling::new)).validate(value ->
            StatScalingMath.nonNegativeFinite(value.threshold) && StatScalingMath.nonNegativeFinite(value.excessFlatRate)
                    && StatScalingMath.nonNegativeFinite(value.excessMultiplierRate)
                    ? DataResult.success(value) : DataResult.error(() -> "Purity scaling values must be finite and non-negative"));

    public double flat(double total) { return StatScalingMath.bloodlineFlat(total, threshold, excessFlatRate); }
    public double multiplier(double total) { return StatScalingMath.bloodlineMultiplier(total, threshold, excessMultiplierRate); }
}
