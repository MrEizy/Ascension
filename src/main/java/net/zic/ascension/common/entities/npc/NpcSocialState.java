package net.zic.ascension.common.entities.npc;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class NpcSocialState {
    private static final int MAX_PLAYERS = 128;
    private final Map<UUID, Relationship> relationships = new LinkedHashMap<>();

    Relationship get(UUID player, int initialRespect) {
        Relationship existing = relationships.get(player);
        if (existing != null) return existing;
        if (relationships.size() >= MAX_PLAYERS) relationships.remove(relationships.keySet().iterator().next());
        Relationship created = new Relationship(initialRespect);
        relationships.put(player, created);
        return created;
    }

    void save(ValueOutput output) {
        StringBuilder serialized = new StringBuilder();
        for (var entry : relationships.entrySet()) {
            if (!serialized.isEmpty()) serialized.append(';');
            Relationship r = entry.getValue();
            serialized.append(entry.getKey()).append(',').append(r.respect).append(',').append(r.talks)
                    .append(',').append(r.challenges).append(',').append(r.withdrawals).append(',').append(r.guidance);
        }
        output.putString("NpcRelationships", serialized.toString());
    }

    void load(ValueInput input) {
        relationships.clear();
        String serialized = input.getStringOr("NpcRelationships", "");
        if (serialized.length() > 16000) return;
        for (String row : serialized.split(";")) {
            String[] parts = row.split(",", 6);
            if (parts.length != 6) continue;
            try {
                UUID player = UUID.fromString(parts[0]);
                Relationship r = new Relationship(Integer.parseInt(parts[1]));
                r.talks = count(Integer.parseInt(parts[2]));
                r.challenges = count(Integer.parseInt(parts[3]));
                r.withdrawals = count(Integer.parseInt(parts[4]));
                r.guidance = count(Integer.parseInt(parts[5]));
                relationships.put(player, r);
                if (relationships.size() >= MAX_PLAYERS) break;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static int count(int value) { return Math.max(0, Math.min(100000, value)); }

    static final class Relationship {
        private int respect;
        private int talks;
        private int challenges;
        private int withdrawals;
        private int guidance;

        Relationship(int respect) { this.respect = Math.max(-100, Math.min(100, respect)); }

        int talks() { return talks; }
        int challenges() { return challenges; }
        int guidance() { return guidance; }
        int respect() { return respect; }

        void talked() { talks = count(talks + 1); }
        void challenged(int change) { challenges = count(challenges + 1); changeRespect(change); }
        void withdrew(int change) { withdrawals = count(withdrawals + 1); changeRespect(change); }
        void guided(int change) { guidance = count(guidance + 1); changeRespect(change); }

        String standing() {
            if (respect <= -40) return "Hostile";
            if (respect <= -15) return "Wary";
            if (respect >= 40) return "Trusted";
            if (respect >= 15) return "Respectful";
            return "Neutral";
        }

        private void changeRespect(int amount) { respect = Math.max(-100, Math.min(100, respect + amount)); }
    }
}
