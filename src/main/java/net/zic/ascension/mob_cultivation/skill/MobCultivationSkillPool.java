package net.zic.ascension.mob_cultivation.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record MobCultivationSkillPool(Identifier id, List<Entry> entries) {
    public record Entry(
            Identifier skill,
            double weight,
            int minimumRealmScore,
            int priority,
            boolean requiresTarget,
            double minimumHealthFraction,
            double maximumHealthFraction,
            double minimumTargetDistance,
            double maximumTargetDistance
    ) {
        public boolean canUse(double healthFraction, double targetDistance, boolean hasTarget) {
            if (requiresTarget && !hasTarget) {
                return false;
            }
            if (healthFraction < minimumHealthFraction || healthFraction > maximumHealthFraction) {
                return false;
            }
            if (!hasTarget) {
                return true;
            }
            return targetDistance >= minimumTargetDistance && targetDistance <= maximumTargetDistance;
        }
    }

    public static MobCultivationSkillPool parse(Identifier id, JsonObject root) {
        List<Entry> entries = new ArrayList<>();
        JsonArray array = root.has("entries") && root.get("entries").isJsonArray()
                ? root.getAsJsonArray("entries")
                : new JsonArray();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            try {
                Identifier skill = Identifier.parse(entry.get("skill").getAsString());
                double weight = number(entry, "weight", 1.0D);
                int minimumRealmScore = Math.max(0, integer(entry, "minimum_realm_score", 0));
                int priority = integer(entry, "priority", 0);
                boolean requiresTarget = bool(entry, "requires_target", true);
                double minimumHealthFraction = clampedFraction(number(entry, "minimum_health_fraction", 0.0D));
                double maximumHealthFraction = clampedFraction(number(entry, "maximum_health_fraction", 1.0D));
                double minimumTargetDistance = Math.max(0.0D, number(entry, "minimum_target_distance", 0.0D));
                double maximumTargetDistance = Math.max(
                        minimumTargetDistance,
                        number(entry, "maximum_target_distance", Double.MAX_VALUE)
                );
                if (Double.isFinite(weight) && weight > 0.0D && minimumHealthFraction <= maximumHealthFraction) {
                    entries.add(new Entry(
                            skill,
                            weight,
                            minimumRealmScore,
                            priority,
                            requiresTarget,
                            minimumHealthFraction,
                            maximumHealthFraction,
                            minimumTargetDistance,
                            maximumTargetDistance
                    ));
                }
            } catch (Exception ignored) {
            }
        }
        return new MobCultivationSkillPool(id, List.copyOf(entries));
    }

    private static double number(JsonObject object, String key, double fallback) {
        return object.has(key) ? object.get(key).getAsDouble() : fallback;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        return object.has(key) ? object.get(key).getAsBoolean() : fallback;
    }

    private static double clampedFraction(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : 0.0D;
    }
}
