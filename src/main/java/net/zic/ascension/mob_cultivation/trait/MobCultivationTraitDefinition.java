package net.zic.ascension.mob_cultivation.trait;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

public record MobCultivationTraitDefinition(
        Identifier id,
        String displayName,
        int regenerationInterval,
        double regenerationFraction,
        double packAlertRadius,
        double retreatHealthThreshold,
        boolean seekHighQi
) {
    public static MobCultivationTraitDefinition parse(Identifier id, JsonObject root) {
        return new MobCultivationTraitDefinition(
                id,
                string(root, "display_name", id.toString()),
                Math.max(20, integer(root, "regeneration_interval", 100)),
                nonNegativeDouble(root, "regeneration_fraction", 0.0D),
                nonNegativeDouble(root, "pack_alert_radius", 0.0D),
                Math.clamp(nonNegativeDouble(root, "retreat_health_threshold", 0.0D), 0.0D, 1.0D),
                bool(root, "seek_high_qi", false)
        );
    }

    private static String string(JsonObject root, String key, String fallback) {
        try {
            return root.has(key) ? root.get(key).getAsString() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int integer(JsonObject root, String key, int fallback) {
        try {
            return root.has(key) ? root.get(key).getAsInt() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double nonNegativeDouble(JsonObject root, String key, double fallback) {
        try {
            double value = root.has(key) ? root.get(key).getAsDouble() : fallback;
            return Double.isFinite(value) && value >= 0.0D ? value : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean bool(JsonObject root, String key, boolean fallback) {
        try {
            return root.has(key) ? root.get(key).getAsBoolean() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
