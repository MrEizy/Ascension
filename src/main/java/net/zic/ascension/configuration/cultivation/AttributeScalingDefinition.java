package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.attribute.AttributeScalingMath;
import net.zic.ascension.api.ascension.core.attribute.RecoveryScaling;

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
            return type.equals("saturation") ? AttributeScalingMath.saturation(value, coefficient, scale)
                    : AttributeScalingMath.power(value, coefficient, scale, exponent);
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
}
