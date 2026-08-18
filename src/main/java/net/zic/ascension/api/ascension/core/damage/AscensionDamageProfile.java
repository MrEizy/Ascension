package net.zic.ascension.api.ascension.core.damage;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageTypeHolder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** the offensive inputs the mod contributes before combat modifiers resolve damage */
public record AscensionDamageProfile(
        double baseDamage,
        double weaponMultiplier,
        Map<Identifier, Double> statScaling,
        Map<Identifier, Double> attributeScaling
) implements RPGEngineDamageTypeHolder {
    public AscensionDamageProfile {
        baseDamage = finiteNonNegative(baseDamage);
        weaponMultiplier = finiteNonNegative(weaponMultiplier);
        statScaling = sanitize(statScaling);
        attributeScaling = sanitize(attributeScaling);
    }

    public static AscensionDamageProfile base(double damage) {
        return new AscensionDamageProfile(damage, 0.0D, Map.of(), Map.of());
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

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }
}
