package net.zic.ascension.common.entities.npc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.zic.ascension.AscensionCraft;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record NpcProfile(List<String> names, List<String> skins, String greeting, List<String> dialogue,
                         List<String> teachings, String offeringItem, int offeringCount, int requiredKills,
                         List<String> taunts, int summonDelayTicks) {
    public static NpcProfile load(ServerLevel level, String type) {
        NpcProfile fallback = new NpcProfile(List.of("Wandering Cultivator"), List.of("default"),
                "Greetings, fellow cultivator.", List.of("The path to immortality is long."),
                List.of(), "ascension:spiritual_stone", 0, 0, List.of(), 240);
        var id = AscensionCraft.prefix("npcs/" + type + ".json");
        try {
            Optional<Resource> resource = level.getServer().getResourceManager().getResource(id);
            if (resource.isEmpty()) return fallback;
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String greeting = json.has("greeting") && json.get("greeting").isJsonPrimitive()
                        ? json.get("greeting").getAsString() : fallback.greeting();
                if (greeting.length() > 480) greeting = greeting.substring(0, 480);
                JsonObject guidance = json.has("guidance") && json.get("guidance").isJsonObject()
                        ? json.getAsJsonObject("guidance") : new JsonObject();
                JsonObject encounter = json.has("encounter") && json.get("encounter").isJsonObject()
                        ? json.getAsJsonObject("encounter") : new JsonObject();
                String offeringItem = string(guidance, "offering_item", fallback.offeringItem());
                if (!offeringItem.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || offeringItem.contains(".."))
                    offeringItem = fallback.offeringItem();
                return new NpcProfile(strings(json, "names", fallback.names(), false),
                        strings(json, "skins", fallback.skins(), true), greeting,
                        strings(json, "dialogue", fallback.dialogue(), false),
                        strings(guidance, "techniques", List.of(), false), offeringItem,
                        bounded(guidance, "offering_count", 0, 64), bounded(guidance, "kills", 0, 64),
                        strings(encounter, "taunts", List.of(), false),
                        bounded(encounter, "summon_delay_ticks", 240, 2400));
            }
        } catch (Exception e) {
            AscensionCraft.LOGGER.warn("Failed to load NPC profile {}", id, e);
            return fallback;
        }
    }

    public String randomName(RandomSource random) { return names.get(random.nextInt(names.size())); }
    public String randomSkin(RandomSource random) { return skins.get(random.nextInt(skins.size())); }
    public String randomDialogue(RandomSource random) { return dialogue.get(random.nextInt(dialogue.size())); }

    private static String string(JsonObject obj, String key, String fallback) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : fallback;
    }

    private static int bounded(JsonObject json, String key, int fallback, int maximum) {
        try {
            return json.has(key) ? Math.max(0, Math.min(maximum, json.get(key).getAsInt())) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static List<String> strings(JsonObject json, String key, List<String> fallback, boolean skin) {
        if (!json.has(key) || !json.get(key).isJsonArray()) return fallback;
        JsonArray array = json.getAsJsonArray(key);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) continue;
            String value = element.getAsString().trim();
            if (value.isEmpty() || value.length() > (skin ? 64 : 240) || value.contains("..")) continue;
            if (skin && !value.matches("[a-z0-9_/-]+")) continue;
            values.add(value);
            if (values.size() == 128) break;
        }
        return values.isEmpty() ? fallback : List.copyOf(values);
    }
}
