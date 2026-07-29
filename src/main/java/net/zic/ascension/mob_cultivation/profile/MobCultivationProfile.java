package net.zic.ascension.mob_cultivation.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record MobCultivationProfile(
        Identifier id,
        int priority,
        MobCultivationProfileTarget target,
        Map<Identifier, Double> foundationPathWeights,
        Map<Identifier, Double> subPathWeights,
        Integer minimumSubPaths,
        Integer maximumSubPaths,
        Set<Identifier> traits,
        Map<Identifier, Double> eliteTraitWeights,
        List<Identifier> skillPools,
        Identifier lootProfile,
        MobCultivationEliteSettings eliteSettings,
        Double statMultiplier,
        Double vitalityBias,
        Double strengthBias,
        Double agilityBias,
        Double spiritBias,
        Double growthMultiplier,
        Double atmosphericQiSensitivity,
        Double lootMultiplier,
        Boolean showParticles,
        Integer initialMajorRealmMin,
        Integer initialMajorRealmMax,
        Integer initialMinorRealmMin,
        Integer initialMinorRealmMax
) {
    public static MobCultivationProfile parse(Identifier id, JsonObject root) {
        JsonObject statBiases = objectOrEmpty(root, "stat_biases");
        JsonObject initialRealm = objectOrEmpty(root, "initial_realm");
        JsonObject subPathCount = objectOrEmpty(root, "sub_path_count");
        JsonObject elite = objectOrEmpty(root, "elite");

        return new MobCultivationProfile(
                id,
                integer(root, "priority", 0),
                MobCultivationProfileTarget.parse(root),
                pathWeights(root, "foundation_path_weights"),
                pathWeights(root, "sub_path_weights"),
                nonNegativeInteger(subPathCount, "min"),
                nonNegativeInteger(subPathCount, "max"),
                identifiers(root, "traits"),
                pathWeights(elite, "trait_weights"),
                identifierList(root, "skill_pools"),
                identifier(root, "loot_profile"),
                eliteSettings(elite),
                nonNegativeDouble(root, "stat_multiplier"),
                nonNegativeDouble(statBiases, "vitality"),
                nonNegativeDouble(statBiases, "strength"),
                nonNegativeDouble(statBiases, "agility"),
                nonNegativeDouble(statBiases, "spirit"),
                nonNegativeDouble(root, "growth_multiplier"),
                nonNegativeDouble(root, "atmospheric_qi_sensitivity"),
                nonNegativeDouble(root, "loot_multiplier"),
                bool(root, "show_particles"),
                nonNegativeInteger(initialRealm, "major_min"),
                nonNegativeInteger(initialRealm, "major_max"),
                nonNegativeInteger(initialRealm, "minor_min"),
                nonNegativeInteger(initialRealm, "minor_max")
        );
    }

    private static MobCultivationEliteSettings eliteSettings(JsonObject elite) {
        if (elite.entrySet().isEmpty()) {
            return null;
        }
        return new MobCultivationEliteSettings(
                boolOr(elite, "enabled", true),
                finiteDouble(elite, "chance", MobCultivationEliteSettings.DEFAULT.chance()),
                finiteDouble(elite, "ancient_chance", MobCultivationEliteSettings.DEFAULT.ancientChance()),
                integer(elite, "extra_trait_rolls", MobCultivationEliteSettings.DEFAULT.extraTraitRolls())
        );
    }

    private static JsonObject objectOrEmpty(JsonObject root, String key) {
        return root.has(key) && root.get(key).isJsonObject()
                ? root.getAsJsonObject(key)
                : new JsonObject();
    }

    private static Map<Identifier, Double> pathWeights(JsonObject root, String key) {
        Map<Identifier, Double> weights = new LinkedHashMap<>();
        JsonObject values = objectOrEmpty(root, key);
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            try {
                double weight = entry.getValue().getAsDouble();
                if (Double.isFinite(weight) && weight >= 0.0D) {
                    weights.put(Identifier.parse(entry.getKey()), weight);
                }
            } catch (Exception ignored) {
            }
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(weights));
    }

    private static Set<Identifier> identifiers(JsonObject root, String key) {
        return Set.copyOf(new LinkedHashSet<>(identifierList(root, key)));
    }

    private static List<Identifier> identifierList(JsonObject root, String key) {
        List<Identifier> values = new ArrayList<>();
        JsonArray array = root.has(key) && root.get(key).isJsonArray()
                ? root.getAsJsonArray(key)
                : new JsonArray();
        for (JsonElement element : array) {
            try {
                values.add(Identifier.parse(element.getAsString()));
            } catch (Exception ignored) {
            }
        }
        return List.copyOf(values);
    }

    private static Identifier identifier(JsonObject root, String key) {
        try {
            return root.has(key) ? Identifier.parse(root.get(key).getAsString()) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int integer(JsonObject root, String key, int fallback) {
        try { return root.has(key) ? root.get(key).getAsInt() : fallback; }
        catch (Exception ignored) { return fallback; }
    }

    private static Integer nonNegativeInteger(JsonObject root, String key) {
        try {
            if (!root.has(key)) return null;
            return Math.max(0, root.get(key).getAsInt());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double nonNegativeDouble(JsonObject root, String key) {
        try {
            if (!root.has(key)) return null;
            double value = root.get(key).getAsDouble();
            return Double.isFinite(value) && value >= 0.0D ? value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double finiteDouble(JsonObject root, String key, double fallback) {
        try {
            double value = root.has(key) ? root.get(key).getAsDouble() : fallback;
            return Double.isFinite(value) ? value : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static Boolean bool(JsonObject root, String key) {
        try { return root.has(key) ? root.get(key).getAsBoolean() : null; }
        catch (Exception ignored) { return null; }
    }

    private static boolean boolOr(JsonObject root, String key, boolean fallback) {
        try { return root.has(key) ? root.get(key).getAsBoolean() : fallback; }
        catch (Exception ignored) { return fallback; }
    }
}
