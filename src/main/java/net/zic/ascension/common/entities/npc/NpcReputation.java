package net.zic.ascension.common.entities.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.zic.ascension.AscensionCraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcReputation extends SavedData {
    private static final Codec<Map<String, Map<String, Integer>>> ENTRIES =
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT));

    public static final SavedDataType<NpcReputation> TYPE = new SavedDataType<>(
            AscensionCraft.prefix("npc_reputation"), NpcReputation::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    ENTRIES.optionalFieldOf("players", Map.of()).forGetter(NpcReputation::savedPlayers)
            ).apply(instance, NpcReputation::new))
    );

    private final Map<String, Map<String, Integer>> players = new HashMap<>();

    public NpcReputation() { }

    private NpcReputation(Map<String, Map<String, Integer>> stored) {
        for (var entry : stored.entrySet()) {
            try {
                UUID.fromString(entry.getKey());
                Map<String, Integer> values = new HashMap<>();
                for (NpcAffiliation faction : NpcAffiliation.values()) {
                    values.put(faction.id(), clamp(entry.getValue().getOrDefault(faction.id(), 0)));
                }
                players.put(entry.getKey(), values);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private Map<String, Map<String, Integer>> savedPlayers() {
        Map<String, Map<String, Integer>> saved = new HashMap<>();
        players.forEach((id, values) -> saved.put(id, Map.copyOf(values)));
        return saved;
    }

    public static NpcReputation get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public int value(UUID player, NpcAffiliation faction) {
        return players.getOrDefault(player.toString(), Map.of()).getOrDefault(faction.id(), 0);
    }

    public int set(UUID player, NpcAffiliation faction, int amount) {
        int updated = clamp(amount);
        players.computeIfAbsent(player.toString(), key -> new HashMap<>()).put(faction.id(), updated);
        setDirty();
        return updated;
    }

    public int add(UUID player, NpcAffiliation faction, int amount) {
        return set(player, faction, (int) Math.max(-100, Math.min(100, (long) value(player, faction) + amount)));
    }

    public void recordKill(ServerPlayer killer, NpcAffiliation victimFaction) {
        UUID player = killer.getUUID();
        switch (victimFaction) {
            case ORTHODOX -> { add(player, NpcAffiliation.ORTHODOX, -12); add(player, NpcAffiliation.UNORTHODOX, 2); add(player, NpcAffiliation.DEMONIC, 4); }
            case UNORTHODOX -> { add(player, NpcAffiliation.ORTHODOX, 4); add(player, NpcAffiliation.UNORTHODOX, -12); add(player, NpcAffiliation.DEMONIC, 1); }
            case DEMONIC -> { add(player, NpcAffiliation.ORTHODOX, 6); add(player, NpcAffiliation.UNORTHODOX, 2); add(player, NpcAffiliation.DEMONIC, -12); }
        }
    }

    private static int clamp(int value) { return Math.max(-100, Math.min(100, value)); }
}
