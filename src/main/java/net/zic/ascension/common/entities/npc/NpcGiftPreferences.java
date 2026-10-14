package net.zic.ascension.common.entities.npc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record NpcGiftPreferences(List<Identifier> preferred, List<Identifier> accepted, List<Identifier> disliked,
                                 int preferredBonus, int waryPercent, int hostilePercent, int trustedRefusalChance,
                                 String preferredReply, String acceptedReply, String refusedReply,
                                 String dislikedReply, String hostileReply, String trustedReply) {
    public enum Response { PREFERRED, ACCEPTED, DISLIKED, UNSUITABLE }

    public static final NpcGiftPreferences DEFAULT = new NpcGiftPreferences(
            List.of(), List.of(Identifier.parse("ascension:spiritual_stone")), List.of(),
            6, 75, 50, 20,
            "A thoughtful offering. I shall remember it.",
            "I accept your gift with thanks.",
            "That is not something I have use for.",
            "You cannot seriously expect me to appreciate that.",
            "After what has happened between us, only a particularly thoughtful offering would be welcome.",
            "Your friendship is worth more than any treasure. Keep it this time.");

    public static NpcGiftPreferences parse(JsonObject obj) {
        if (obj == null) return DEFAULT;
        return new NpcGiftPreferences(
                items(obj, "preferred", DEFAULT.preferred()),
                items(obj, "accepted", DEFAULT.accepted()),
                items(obj, "disliked", DEFAULT.disliked()),
                bounded(obj, "preferred_bonus", DEFAULT.preferredBonus(), 0, 25),
                bounded(obj, "wary_percent", DEFAULT.waryPercent(), 1, 100),
                bounded(obj, "hostile_percent", DEFAULT.hostilePercent(), 1, 100),
                bounded(obj, "trusted_refusal_chance", DEFAULT.trustedRefusalChance(), 0, 100),
                line(obj, "preferred_reply", DEFAULT.preferredReply()),
                line(obj, "accepted_reply", DEFAULT.acceptedReply()),
                line(obj, "refused_reply", DEFAULT.refusedReply()),
                line(obj, "disliked_reply", DEFAULT.dislikedReply()),
                line(obj, "hostile_reply", DEFAULT.hostileReply()),
                line(obj, "trusted_reply", DEFAULT.trustedReply()));
    }

    public Response response(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (disliked.contains(id)) return Response.DISLIKED;
        if (preferred.contains(id)) return Response.PREFERRED;
        if (accepted.contains(id)) return Response.ACCEPTED;
        return Response.UNSUITABLE;
    }

    public String hint() {
        List<String> items = new ArrayList<>();
        for (Identifier id : preferred) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null) items.add(new ItemStack(item).getHoverName().getString());
            if (items.size() >= 3) break;
        }
        String examples = items.isEmpty() ? "Spiritual Stone" : String.join(", ", items);
        String hint = "Prefers: " + examples + ". Hold a gift, or use empty hands for a Spiritual Stone.";
        return hint.length() <= 220 ? hint : hint.substring(0, 217) + "...";
    }

    private static List<Identifier> items(JsonObject obj, String key, List<Identifier> fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) return fallback;
        List<Identifier> result = new ArrayList<>();
        for (JsonElement element : obj.getAsJsonArray(key)) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) continue;
            String value = element.getAsString();
            if (!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || value.contains("..")) continue;
            try {
                Identifier id = Identifier.parse(value);
                if (BuiltInRegistries.ITEM.getValue(id) != null && !result.contains(id)) result.add(id);
            } catch (IllegalArgumentException ignored) {
            }
            if (result.size() >= 16) break;
        }
        return List.copyOf(result);
    }

    private static int bounded(JsonObject obj, String key, int fallback, int min, int max) {
        try {
            return obj.has(key) ? Math.clamp(obj.get(key).getAsInt(), min, max) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String line(JsonObject obj, String key, String fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive() || !obj.get(key).getAsJsonPrimitive().isString()) return fallback;
        String value = obj.get(key).getAsString().trim();
        return value.isBlank() || value.length() > 240 ? fallback : value;
    }
}
