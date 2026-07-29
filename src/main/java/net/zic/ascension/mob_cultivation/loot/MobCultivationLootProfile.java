package net.zic.ascension.mob_cultivation.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record MobCultivationLootProfile(Identifier id, List<Entry> entries) {
    public record Entry(
            Identifier item,
            double weight,
            int minimumRealmScore,
            int maximumRealmScore,
            int minimumCount,
            int maximumCount,
            MobCultivationEliteTier minimumEliteTier,
            Set<Identifier> requiredTraits,
            Set<Identifier> requiredSubPaths
    ) {
        boolean matches(MobCultivationData data, int realmScore) {
            return realmScore >= minimumRealmScore
                    && (maximumRealmScore < 0 || realmScore <= maximumRealmScore)
                    && data.getEliteTier().ordinal() >= minimumEliteTier.ordinal()
                    && data.getTraits().containsAll(requiredTraits)
                    && data.getSubPaths().containsAll(requiredSubPaths);
        }
    }

    public static MobCultivationLootProfile parse(Identifier id, JsonObject root) {
        List<Entry> entries = new ArrayList<>();
        JsonArray array = root.has("entries") && root.get("entries").isJsonArray() ? root.getAsJsonArray("entries") : new JsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject entry = element.getAsJsonObject();
            try {
                Identifier item = Identifier.parse(entry.get("item").getAsString());
                double weight = entry.has("weight") ? entry.get("weight").getAsDouble() : 1.0D;
                int minRealm = integer(entry, "minimum_realm_score", 0);
                int maxRealm = integer(entry, "maximum_realm_score", -1);
                int minCount = Math.max(1, integer(entry, "minimum_count", 1));
                int maxCount = Math.max(minCount, integer(entry, "maximum_count", minCount));
                MobCultivationEliteTier elite = MobCultivationEliteTier.parse(string(entry, "minimum_elite_tier", "normal"));
                if (Double.isFinite(weight) && weight > 0.0D) {
                    entries.add(new Entry(
                            item,
                            weight,
                            Math.max(0, minRealm),
                            maxRealm,
                            minCount,
                            maxCount,
                            elite,
                            identifiers(entry, "required_traits"),
                            identifiers(entry, "required_sub_paths")
                    ));
                }
            } catch (Exception ignored) {
            }
        }
        return new MobCultivationLootProfile(id, List.copyOf(entries));
    }

    private static int integer(JsonObject root, String key, int fallback) {
        try { return root.has(key) ? root.get(key).getAsInt() : fallback; }
        catch (Exception ignored) { return fallback; }
    }

    private static String string(JsonObject root, String key, String fallback) {
        try { return root.has(key) ? root.get(key).getAsString() : fallback; }
        catch (Exception ignored) { return fallback; }
    }

    private static Set<Identifier> identifiers(JsonObject root, String key) {
        Set<Identifier> result = new LinkedHashSet<>();
        if (!root.has(key) || !root.get(key).isJsonArray()) return Set.of();
        for (JsonElement element : root.getAsJsonArray(key)) {
            try { result.add(Identifier.parse(element.getAsString())); }
            catch (Exception ignored) { }
        }
        return Set.copyOf(result);
    }
}
