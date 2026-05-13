package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;

public class DebuffSkillData implements IPersistentSkillData {

    // -1 means permanent until something else removes it
    private long expiresAtGameTime = -1L;
    private long appliedAtGameTime = -1L;

    public DebuffSkillData() {}

    public DebuffSkillData(long expiresAtGameTime) {
        this.expiresAtGameTime = expiresAtGameTime;

    }

    public DebuffSkillData(long expiresAtGameTime, long appliedAtGameTime) {
        this.expiresAtGameTime = expiresAtGameTime;
        this.appliedAtGameTime = appliedAtGameTime;
    }

    public static DebuffSkillData permanent() {
        return new DebuffSkillData(-1L);
    }

    public static DebuffSkillData permanent(long appliedAtGameTime) {
        return new DebuffSkillData(-1L, appliedAtGameTime);
    }

    public static DebuffSkillData temporary(long expiresAtGameTime) {
        return new DebuffSkillData(expiresAtGameTime);
    }

    public static DebuffSkillData temporary(long expiresAtGameTime, long appliedAtGameTime) {
        return new DebuffSkillData(expiresAtGameTime, appliedAtGameTime);
    }

    public long getExpiresAtGameTime() {
        return expiresAtGameTime;
    }

    public long getAppliedAtGameTime() {
        return appliedAtGameTime;
    }

    public void setExpiresAtGameTime(long expiresAtGameTime) {
        this.expiresAtGameTime = expiresAtGameTime;
    }

    public boolean isPermanent() {
        return expiresAtGameTime < 0L;
    }

    public boolean isExpired(long gameTime) {
        return !isPermanent() && gameTime >= expiresAtGameTime;
    }

    public long getElapsedTicks(long currentGameTime) {
        if (appliedAtGameTime < 0L) return 0L;
        return Math.max(0L, currentGameTime - appliedAtGameTime);
    }

    public DebuffSkillData mergeWith(DebuffSkillData other) {
        if (other == null) return copyData();

        long mergedAppliedAt;

        if (this.appliedAtGameTime < 0L) {
            mergedAppliedAt = other.appliedAtGameTime;
        } else if (other.appliedAtGameTime < 0L) {
            mergedAppliedAt = this.appliedAtGameTime;
        } else {
            mergedAppliedAt = Math.min(this.appliedAtGameTime, other.appliedAtGameTime);
        }

        if (this.isPermanent() || other.isPermanent()) {
            return new DebuffSkillData(-1L, mergedAppliedAt);
        }

        return new DebuffSkillData(
                Math.max(this.expiresAtGameTime, other.expiresAtGameTime),
                mergedAppliedAt
        );
    }

    public DebuffSkillData copyData() {
        return new DebuffSkillData(this.expiresAtGameTime, this.appliedAtGameTime);
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("expires_at_game_time", expiresAtGameTime);
        tag.putLong("applied_at_game_time", appliedAtGameTime);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(expiresAtGameTime);
        buf.writeLong(appliedAtGameTime);
    }

    public static DebuffSkillData decode(RegistryFriendlyByteBuf buf) {
        long expiresAt = buf.readLong();
        long appliedAt = buf.readLong();

        return new DebuffSkillData(expiresAt, appliedAt);
    }

    public static DebuffSkillData read(CompoundTag tag) {
        long expiresAt = tag.getLong("expires_at_game_time");
        long appliedAt = tag.contains("applied_at_game_time")
                ? tag.getLong("applied_at_game_time")
                : -1L;

        return new DebuffSkillData(expiresAt, appliedAt);
    }

    @Override
    public IPersistentSkillData copy() {
        return copyData();
    }

    @Override
    public IPersistentSkillData merge(IPersistentSkillData other) {
        if (other instanceof DebuffSkillData otherDebuffData) {
            return mergeWith(otherDebuffData);
        }

        return copy();
    }
}