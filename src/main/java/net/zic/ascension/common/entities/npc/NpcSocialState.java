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
                    .append(',').append(r.challenges).append(',').append(r.withdrawals).append(',').append(r.guidance)
                    .append(',').append(r.lastGiftTick).append(',').append(r.lastInsultTick)
                    .append(',').append(r.lastAttackTick);
        }
        output.putString("NpcRelationships", serialized.toString());
    }

    void load(ValueInput input) {
        relationships.clear();
        String serialized = input.getStringOr("NpcRelationships", "");
        if (serialized.length() > 16000) return;
        for (String row : serialized.split(";")) {
            String[] parts = row.split(",", 9);
            if (parts.length != 6 && parts.length != 9) continue;
            try {
                UUID player = UUID.fromString(parts[0]);
                Relationship r = new Relationship(Integer.parseInt(parts[1]));
                r.talks = count(Integer.parseInt(parts[2]));
                r.challenges = count(Integer.parseInt(parts[3]));
                r.withdrawals = count(Integer.parseInt(parts[4]));
                r.guidance = count(Integer.parseInt(parts[5]));
                if (parts.length >= 9) {
                    r.lastGiftTick = Long.parseLong(parts[6]);
                    r.lastInsultTick = Long.parseLong(parts[7]);
                    r.lastAttackTick = Long.parseLong(parts[8]);
                }
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
        private long lastGiftTick = -1;
        private long lastInsultTick = -1;
        private long lastAttackTick = -1;

        Relationship(int respect) { this.respect = Math.max(-100, Math.min(100, respect)); }

        int talks() { return talks; }
        int challenges() { return challenges; }
        int guidance() { return guidance; }
        int respect() { return respect; }
        int setRespect(int value) { respect = Math.max(-100, Math.min(100, value)); return respect; }
        int addRespect(int value) { changeRespect(value); return respect; }

        boolean gifted(long now, int change) {
            if (!ready(now, lastGiftTick, 600)) return false;
            lastGiftTick = now;
            changeRespect(change);
            return true;
        }

        boolean insulted(long now, int change) {
            if (!ready(now, lastInsultTick, 1200)) return false;
            lastInsultTick = now;
            changeRespect(change);
            return true;
        }

        boolean attacked(long now, int change) {
            if (!ready(now, lastAttackTick, 400)) return false;
            lastAttackTick = now;
            changeRespect(change);
            return true;
        }

        void helped(int change) { changeRespect(change); }

        boolean canGift(long now) { return ready(now, lastGiftTick, 600); }
        boolean canInsult(long now) { return ready(now, lastInsultTick, 1200); }

        private static boolean ready(long now, long previous, long cooldown) {
            return previous < 0 || now < previous || now - previous >= cooldown;
        }

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
