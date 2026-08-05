package net.zic.ascension.mob_cultivation.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record MobCultivationSkillPool(Identifier id, List<Entry> entries) {
    public record Entry(Identifier skill, double weight, int minimumRealmScore) {
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
                double weight = entry.has("weight") ? entry.get("weight").getAsDouble() : 1.0D;
                int minimumRealmScore = entry.has("minimum_realm_score")
                        ? Math.max(0, entry.get("minimum_realm_score").getAsInt())
                        : 0;
                if (Double.isFinite(weight) && weight > 0.0D) {
                    entries.add(new Entry(skill, weight, minimumRealmScore));
                }
            } catch (Exception ignored) {
            }
        }
        return new MobCultivationSkillPool(id, List.copyOf(entries));
    }
}
