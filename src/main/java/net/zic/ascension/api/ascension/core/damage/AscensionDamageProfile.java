package net.zic.ascension.api.ascension.core.damage;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageTypeHolder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record AscensionDamageProfile(
        double baseDamage,
        double weaponMultiplier,
        Map<Identifier, Double> statScaling,
        Map<Identifier, Double> attributeScaling,
        Optional<Double> minimum,
        Optional<Double> maximum
) implements RPGEngineDamageTypeHolder {
    public AscensionDamageProfile {
        baseDamage = finiteNonNegative(baseDamage);
        weaponMultiplier = finiteNonNegative(weaponMultiplier);
        statScaling = sanitize(statScaling);
        attributeScaling = sanitize(attributeScaling);
        minimum = sanitizeBound(minimum);
        maximum = sanitizeBound(maximum);
    }

    public static AscensionDamageProfile base(double damage) {
        return new AscensionDamageProfile(
                damage,
                0.0D,
                Map.of(),
                Map.of(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public double clamp(double damage) {
        double minimumValue = minimum.orElse(Double.NEGATIVE_INFINITY);
        double maximumValue = maximum.orElse(Double.POSITIVE_INFINITY);
        if (minimumValue > maximumValue) {
            maximumValue = minimumValue;
        }
        return Math.max(minimumValue, Math.min(maximumValue, damage));
    }

    private static Map<Identifier, Double> sanitize(Map<Identifier, Double> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> sanitized = new LinkedHashMap<>();
        values.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value) && Math.abs(value) > 1.0E-12D) {
                sanitized.put(id, value);
            }
        });
        return Collections.unmodifiableMap(sanitized);
    }

    private static Optional<Double> sanitizeBound(Optional<Double> value) {
        if (value == null || value.isEmpty() || !Double.isFinite(value.get())) {
            return Optional.empty();
        }
        return Optional.of(value.get());
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }
}
