package net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;

public class BloodfeastTechniqueData implements ITechniqueData {

    private static final ResourceLocation PLAYER_ID =
            ResourceLocation.withDefaultNamespace("player");

    // ── Persisted fields ──────────────────────────────────────────────────────

    /** Total kills since the technique was first equipped. */
    private int totalKills;

    /** Subset of totalKills that were player kills. */
    private int totalPlayerKills;

    /** Snapshot of totalKills at the moment the most recent gate was cleared. */
    private int killsAtLastGate;

    /** Snapshot of totalPlayerKills at the moment the most recent gate was cleared. */
    private int playerKillsAtLastGate;

    /**
     * How many realm gates have been cleared with an exclusively player-kill
     * window.  Each adds +0.2 minor / +0.4 major to the stat bonus.
     */
    private int playerOnlyGatesCleared;

    // ── Constructors ─────────────────────────────────────────────────────────

    public BloodfeastTechniqueData() {}

    public BloodfeastTechniqueData(CompoundTag tag) {
        totalKills             = tag.getInt("total_kills");
        totalPlayerKills       = tag.getInt("total_player_kills");
        killsAtLastGate        = tag.getInt("kills_at_last_gate");
        playerKillsAtLastGate  = tag.getInt("player_kills_at_last_gate");
        playerOnlyGatesCleared = tag.getInt("player_only_gates");
    }

    public BloodfeastTechniqueData(RegistryFriendlyByteBuf buf) {
        totalKills             = buf.readInt();
        totalPlayerKills       = buf.readInt();
        killsAtLastGate        = buf.readInt();
        playerKillsAtLastGate  = buf.readInt();
        playerOnlyGatesCleared = buf.readInt();
    }

    public void recordKill(ResourceLocation entityType) {
        totalKills++;
        if (PLAYER_ID.equals(entityType)) {
            totalPlayerKills++;
        }
    }

    // ── Gate threshold table ──────────────────────────────────────────────────

    /**
     * Minimum total-kill count required before the player may cultivate into
     * the given major realm.  Realms 0 and 1 are freely accessible.
     *
     * Realm 2  → 100    (first gate)
     * Realm 3  → 1000   (second gate)
     * Realm 4  → 1250   (third gate)
     * Realm 5  → 1500   (fourth gate)
     * Realm 6  → 2000   (fifth gate)
     */
    public static int getKillThreshold(int majorRealm) {
        return switch (majorRealm) {
            case 2 -> 100;
            case 3 -> 1000;
            case 4 -> 1250;
            case 5 -> 1500;
            case 6 -> 2000;
            default -> 0;
        };
    }

    public boolean hasMetKillRequirement(int majorRealm) {
        return totalKills >= getKillThreshold(majorRealm);
    }

    // ── Gate-clear callback ───────────────────────────────────────────────────

    /**
     * Must be called by the technique exactly once when the kill gate for
     * {@code majorRealm} is confirmed open (i.e., the breakthrough actually
     * fires, not just when cultivation is checked).
     *
     * Determines whether every kill since the previous gate was a player
     * kill; if so increments the bonus counter.  Then snapshots current totals
     * so the next gate has a clean reference window.
     */
    public void onGateCleared(int majorRealm) {
        int killsSinceLast       = totalKills       - killsAtLastGate;
        int playerKillsSinceLast = totalPlayerKills - playerKillsAtLastGate;

        if (killsSinceLast > 0 && killsSinceLast == playerKillsSinceLast) {
            playerOnlyGatesCleared++;
        }

        killsAtLastGate       = totalKills;
        playerKillsAtLastGate = totalPlayerKills;
    }


    public double getMinorRealmStatBonus() {
        return playerOnlyGatesCleared * 0.2D;
    }

    public double getMajorRealmStatBonus() {
        return playerOnlyGatesCleared * 0.4D;
    }

    // ── Convenience accessors ─────────────────────────────────────────────────

    public int getTotalKills()             { return totalKills; }
    public int getTotalPlayerKills()       { return totalPlayerKills; }
    public int getPlayerOnlyGatesCleared() { return playerOnlyGatesCleared; }

    /** Progress (0.0–1.0) toward the kill threshold for the given realm. */
    public double getGateProgress(int majorRealm) {
        int threshold = getKillThreshold(majorRealm);
        if (threshold == 0) return 1.0D;
        return Math.min(1.0D, (double) totalKills / threshold);
    }

    // ── IDataInstance ─────────────────────────────────────────────────────────

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("total_kills",               totalKills);
        tag.putInt("total_player_kills",        totalPlayerKills);
        tag.putInt("kills_at_last_gate",        killsAtLastGate);
        tag.putInt("player_kills_at_last_gate", playerKillsAtLastGate);
        tag.putInt("player_only_gates",         playerOnlyGatesCleared);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(totalKills);
        buf.writeInt(totalPlayerKills);
        buf.writeInt(killsAtLastGate);
        buf.writeInt(playerKillsAtLastGate);
        buf.writeInt(playerOnlyGatesCleared);
    }
}