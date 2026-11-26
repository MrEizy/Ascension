package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record AttributeScalingDefinition(Map<Identifier, List<Curve>> common,
                                         Map<Identifier, List<Curve>> player,
                                         double excessArmorResistance, RecoveryScaling recovery) {
    public AttributeScalingDefinition(Map<Identifier, List<Curve>> common, Map<Identifier, List<Curve>> player, double excessArmorResistance) {
        this(common, player, excessArmorResistance, RecoveryScaling.DEFAULT);
    }

    private static final Codec<RecoveryScaling> RECOVERY_CODEC = RecordCodecBuilder.<double[]>create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("minimum_recovery_seconds", 60.0D).forGetter(v -> v[0]),
            Codec.DOUBLE.optionalFieldOf("maximum_recovery_seconds", 1200.0D).forGetter(v -> v[1]),
            Codec.DOUBLE.optionalFieldOf("health_scaling_threshold", 100000.0D).forGetter(v -> v[2]),
            Codec.DOUBLE.optionalFieldOf("sleep_missing_health_fraction", 0.25D).forGetter(v -> v[3])
    ).apply(instance, (a, b, c, d) -> new double[]{a, b, c, d})).comapFlatMap(v -> {
        try {
            return DataResult.success(new RecoveryScaling(v[0], v[1], v[2], v[3]));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }, v -> new double[]{v.minimumSeconds(), v.maximumSeconds(), v.healthThreshold(), v.sleepFraction()});

    public record Curve(Identifier stat, String type, double coefficient, double scale, double exponent) {
        public static final Codec<Curve> CODEC = RecordCodecBuilder.<Curve>create(instance -> instance.group(
                Identifier.CODEC.fieldOf("stat").forGetter(Curve::stat),
                Codec.STRING.fieldOf("type").forGetter(Curve::type),
                Codec.DOUBLE.fieldOf("coefficient").forGetter(Curve::coefficient),
                Codec.DOUBLE.optionalFieldOf("scale", 100.0D).forGetter(Curve::scale),
                Codec.DOUBLE.optionalFieldOf("exponent", 1.0D).forGetter(Curve::exponent)
        ).apply(instance, Curve::new)).validate(curve -> curve.valid() ? DataResult.success(curve)
                : DataResult.error(() -> "Attribute curve requires power or saturation, a non-negative finite coefficient, positive finite scale and exponent"));

        private boolean valid() {
            return (type.equals("power") || type.equals("saturation"))
                    && Double.isFinite(coefficient) && coefficient >= 0
                    && Double.isFinite(scale) && scale >= 1.0E-9D
                    && Double.isFinite(exponent) && exponent > 0 && exponent <= 4;
        }

        public double at(double value) {
            return type.equals("saturation") ? saturation(value, coefficient, scale)
                    : power(value, coefficient, scale, exponent);
        }
    }

    private static final Codec<Map<Identifier, List<Curve>>> CURVES = Codec.unboundedMap(Identifier.CODEC, Curve.CODEC.listOf());
    public static final Codec<AttributeScalingDefinition> CODEC = RecordCodecBuilder.<AttributeScalingDefinition>create(instance -> instance.group(
            CURVES.optionalFieldOf("common", Map.of()).forGetter(AttributeScalingDefinition::common),
            CURVES.optionalFieldOf("player", Map.of()).forGetter(AttributeScalingDefinition::player),
            Codec.DOUBLE.optionalFieldOf("excess_armor_resistance", 100.0D).forGetter(AttributeScalingDefinition::excessArmorResistance),
            RECOVERY_CODEC.optionalFieldOf("recovery", RecoveryScaling.DEFAULT).forGetter(AttributeScalingDefinition::recovery)
    ).apply(instance, AttributeScalingDefinition::new)).validate(profile ->
            Double.isFinite(profile.excessArmorResistance) && profile.excessArmorResistance >= 1.0E-9D
                    && java.util.Collections.disjoint(profile.common.keySet(), profile.player.keySet())
                    ? DataResult.success(profile) : DataResult.error(() -> "Armour resistance must be positive and finite; common and player attribute keys must not overlap"));

    private static final double MAX_CONTRIBUTION = Float.MAX_VALUE / 1024.0D;

    private static double power(double stat, double coefficient, double scale, double exponent) {
        double x = positiveStat(stat);
        if (x == 0 || coefficient == 0) return 0;
        double ratio = x / scale;
        double logTerm = Double.isFinite(ratio) ? Math.log1p(ratio) : Math.log(x) - Math.log(scale);
        return finiteContribution(Math.exp(Math.log(coefficient) + Math.log(x)
                + (exponent - 1.0D) * logTerm));
    }

    private static double saturation(double stat, double coefficient, double scale) {
        double x = positiveStat(stat);
        if (x == 0 || coefficient == 0) return 0;
        return finiteContribution(coefficient / (1.0D + scale / x));
    }

    private static double positiveStat(double stat) {
        return Double.isFinite(stat) ? Math.max(0, stat) : 0;
    }

    public static double finiteContribution(double value) {
        return Double.isNaN(value) ? 0 : Math.clamp(value, 0, MAX_CONTRIBUTION);
    }

    public record RecoveryScaling(double minimumSeconds, double maximumSeconds,
                                  double healthThreshold, double sleepFraction) {
        public static final RecoveryScaling DEFAULT = new RecoveryScaling(60, 1200, 100000, 0.25);

        public RecoveryScaling {
            if (!Double.isFinite(minimumSeconds) || minimumSeconds < 1
                    || !Double.isFinite(maximumSeconds) || maximumSeconds < minimumSeconds
                    || !Double.isFinite(healthThreshold) || healthThreshold <= 0
                    || !Double.isFinite(sleepFraction) || sleepFraction < 0 || sleepFraction > 1) {
                throw new IllegalArgumentException("Invalid recovery scaling: expected 1 <= minimum <= maximum, positive threshold, and sleep fraction in [0, 1]");
            }
        }

        public double recoverySeconds(double maximumHealth) {
            if (!Double.isFinite(maximumHealth) || maximumHealth <= 0) return minimumSeconds;
            double fraction = 1.0 / (1.0 + healthThreshold / maximumHealth);
            return minimumSeconds + (maximumSeconds - minimumSeconds) * fraction;
        }

        public double healthPerSecond(double maximumHealth) {
            if (!Double.isFinite(maximumHealth) || maximumHealth <= 0) return 0;
            return maximumHealth / recoverySeconds(maximumHealth);
        }

        public double sleepHealing(double maximumHealth, double currentHealth) {
            if (!Double.isFinite(maximumHealth) || !Double.isFinite(currentHealth) || currentHealth <= 0) return 0;
            return Math.max(0, maximumHealth - currentHealth) * sleepFraction;
        }
    }
}
