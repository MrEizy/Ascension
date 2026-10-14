package net.zic.ascension.common.entities.npc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.zic.ascension.AscensionCraft;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record NpcProfile(List<String> names, List<String> skins, String greeting, List<String> dialogue,
                         List<String> teachings, String offeringItem, int offeringCount, int requiredKills,
                         List<String> taunts, int summonDelayTicks, List<Personality> personalities, List<AffiliationWeight> affiliations) {
    private static ResourceManager cachedManager;
    private static Map<String, Personality> definitions = Map.of();
    private static final Map<String, NpcProfile> profiles = new HashMap<>();

    public static synchronized NpcProfile load(ServerLevel level, String type) {
        ResourceManager manager = level.getServer().getResourceManager();
        if (manager != cachedManager) {
            cachedManager = manager;
            profiles.clear();
            definitions = loadDefinitions(manager);
        }
        return profiles.computeIfAbsent(type, key -> loadProfile(manager, key));
    }

    private static NpcProfile loadProfile(ResourceManager manager, String type) {
        NpcProfile fallback = new NpcProfile(List.of("Wandering Cultivator"), List.of("default"),
                "Greetings, fellow cultivator.", List.of("The path to immortality is long."),
                List.of(), "ascension:spiritual_stone", 0, 0, List.of(), 240, List.of(Personality.DEFAULT), List.of(new AffiliationWeight(NpcAffiliation.ORTHODOX, 1)));
        var id = AscensionCraft.prefix("npcs/" + type + ".json");
        try {
            Optional<Resource> resource = manager.getResource(id);
            if (resource.isEmpty()) return fallback;
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String greeting = json.has("greeting") && json.get("greeting").isJsonPrimitive() ? json.get("greeting").getAsString() : fallback.greeting();
                if (greeting.length() > 480) greeting = greeting.substring(0, 480);
                JsonObject guidance = json.has("guidance") && json.get("guidance").isJsonObject() ? json.getAsJsonObject("guidance") : new JsonObject();
                JsonObject encounter = json.has("encounter") && json.get("encounter").isJsonObject() ? json.getAsJsonObject("encounter") : new JsonObject();
                String offeringItem = string(guidance, "offering_item", fallback.offeringItem());
                if (!offeringItem.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || offeringItem.contains(".."))
                    offeringItem = fallback.offeringItem();
                return new NpcProfile(strings(json, "names", fallback.names(), false),
                        strings(json, "skins", fallback.skins(), true), greeting,
                        strings(json, "dialogue", fallback.dialogue(), false),
                        strings(guidance, "techniques", List.of(), false), offeringItem,
                        bounded(guidance, "offering_count", 0, 64), bounded(guidance, "kills", 0, 64),
                        strings(encounter, "taunts", List.of(), false),
                        bounded(encounter, "summon_delay_ticks", 240, 2400), personalities(json), affiliations(json));
            }
        } catch (Exception e) {
            AscensionCraft.LOGGER.warn("Failed to load NPC profile {}", id, e);
            return fallback;
        }
    }

    private static Map<String, Personality> loadDefinitions(ResourceManager manager) {
        var id = AscensionCraft.prefix("npcs/npc_personalities.json");
        Map<String, Personality> loaded = new LinkedHashMap<>();
        try {
            Optional<Resource> resource = manager.getResource(id);
            if (resource.isEmpty()) {
                AscensionCraft.LOGGER.warn("Missing global NPC personalities: {}", id);
                return Map.of();
            }
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (!json.has("personalities") || !json.get("personalities").isJsonObject()) return Map.of();
                for (var entry : json.getAsJsonObject("personalities").entrySet()) {
                    String key = entry.getKey();
                    if (!key.matches("[a-z0-9_]{1,48}") || !entry.getValue().isJsonObject()) continue;
                    loaded.put(key, parsePersonality(key, entry.getValue().getAsJsonObject(), 1));
                    if (loaded.size() >= 64) break;
                }
            }
        } catch (Exception e) {
            AscensionCraft.LOGGER.warn("Failed to load global NPC personalities {}", id, e);
        }
        return Map.copyOf(loaded);
    }

    public record AffiliationWeight(NpcAffiliation affiliation, int weight) { }

    private static List<AffiliationWeight> affiliations(JsonObject json) {
        if (!json.has("affiliations") || !json.get("affiliations").isJsonArray())
            return List.of(new AffiliationWeight(NpcAffiliation.ORTHODOX, 1));
        List<AffiliationWeight> available = new ArrayList<>();
        for (JsonElement value : json.getAsJsonArray("affiliations")) {
            if (!value.isJsonObject()) continue;
            JsonObject item = value.getAsJsonObject();
            NpcAffiliation faction = NpcAffiliation.parse(string(item, "id", ""));
            if (faction == null || available.stream().anyMatch(entry -> entry.affiliation() == faction)) continue;
            available.add(new AffiliationWeight(faction, Math.max(1, bounded(item, "weight", 1, 100))));
        }
        return available.isEmpty() ? List.of(new AffiliationWeight(NpcAffiliation.ORTHODOX, 1)) : List.copyOf(available);
    }

    public NpcAffiliation randomAffiliation(RandomSource random) {
        int total = 0;
        for (AffiliationWeight entry : affiliations) total += entry.weight();
        int roll = random.nextInt(total);
        for (AffiliationWeight entry : affiliations) {
            roll -= entry.weight();
            if (roll < 0) return entry.affiliation();
        }
        return affiliations.getFirst().affiliation();
    }

    public Personality randomPersonality(RandomSource random) {
        int total = 0;
        for (Personality personality : personalities) total += personality.weight();
        int roll = random.nextInt(total);
        for (Personality personality : personalities) {
            roll -= personality.weight();
            if (roll < 0) return personality;
        }
        return personalities.getFirst();
    }

    public Personality personality(String id) {
        for (Personality personality : personalities) {
            if (personality.id().equals(id)) return personality;
        }
        return personalities.getFirst();
    }

    public static String canonicalPersonalityId(String id) {
        return switch (id) {
            case "earnest", "patient", "benevolent" -> "compassionate";
            case "observant", "dignified", "distant" -> "reserved";
            case "unyielding" -> "competitive";
            case "vain" -> "arrogant";
            default -> id;
        };
    }

    private static List<Personality> personalities(JsonObject json) {
        if (!json.has("personalities") || !json.get("personalities").isJsonArray()) return List.of(Personality.DEFAULT);
        List<Personality> selected = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("personalities")) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            String id = canonicalPersonalityId(string(obj, "id", ""));
            Personality definition = definitions.get(id);
            if (definition == null || selected.stream().anyMatch(p -> p.id().equals(id))) continue;
            selected.add(definition.withWeight(Math.max(1, bounded(obj, "weight", 1, 100))));
            if (selected.size() >= 12) break;
        }
        return selected.isEmpty() ? List.of(Personality.DEFAULT) : List.copyOf(selected);
    }

    private static Personality parsePersonality(String id, JsonObject obj, int weight) {
        String label = string(obj, "name", id.replace('_', ' '));
        if (label.isBlank() || label.length() > 48) label = id;
        String greeting = string(obj, "greeting", "");
        if (greeting.length() > 240) greeting = greeting.substring(0, 240);
        return new Personality(id, label, weight,
                Math.max(-100, Math.min(100, signed(obj, "initial_respect", 0))),
                Math.max(-25, Math.min(25, signed(obj, "challenge_respect", -5))),
                Math.max(-25, Math.min(25, signed(obj, "withdraw_respect", -2))),
                Math.max(-25, Math.min(25, signed(obj, "guidance_respect", 15))),
                greeting, strings(obj, "dialogue", List.of(), false),
                strings(obj, "stronger", List.of(), false), strings(obj, "weaker", List.of(), false),
                strings(obj, "familiar", List.of(), false), strings(obj, "challenged", List.of(), false),
                strings(obj, "guided", List.of(), false), strings(obj, "friendly", List.of(), false),
                strings(obj, "hostile", List.of(), false), strings(obj, "taunts", List.of(), false),
                signed(obj, "gift_respect", 6), signed(obj, "insult_respect", -8),
                signed(obj, "aid_respect", 8), signed(obj, "attack_respect", -12));
    }

    private static int signed(JsonObject json, String key, int fallback) {
        try {
            return json.has(key) ? json.get(key).getAsInt() : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public record Personality(String id, String name, int weight, int initialRespect, int challengeRespect,
                              int withdrawRespect, int guidanceRespect, String greeting, List<String> dialogue,
                              List<String> stronger, List<String> weaker, List<String> familiar,
                              List<String> challenged, List<String> guided, List<String> friendly,
                              List<String> hostile, List<String> taunts, int giftRespect, int insultRespect,
                              int aidRespect, int attackRespect) {
        Personality withWeight(int newWeight) {
            return new Personality(id, name, newWeight, initialRespect, challengeRespect,
                    withdrawRespect, guidanceRespect, greeting, dialogue, stronger, weaker,
                    familiar, challenged, guided, friendly, hostile, taunts,
                    giftRespect, insultRespect, aidRespect, attackRespect);
        }

        private static final Personality DEFAULT = new Personality("neutral", "Neutral", 1, 0, -5, -2, 15,
                "", List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), 6, -8, 8, -12);
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
