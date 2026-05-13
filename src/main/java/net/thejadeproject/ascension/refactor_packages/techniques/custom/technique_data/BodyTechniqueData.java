package net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;

public class BodyTechniqueData implements ITechniqueData {
    public static final long MERGE_COOLDOWN_TICKS = 24000L;
    public static final long PROFICIENCY_THRESHOLD = 6000L;

    private long totalCultivationTicks;
    private long lastFailedMergeWorldTime = -1L;

    public BodyTechniqueData() {}

    private BodyTechniqueData(long totalCultivationTicks, long lastFailedMergeWorldTime) {
        this.totalCultivationTicks = totalCultivationTicks;
        this.lastFailedMergeWorldTime = lastFailedMergeWorldTime;
    }

    public void incrementCultivationTicks() { totalCultivationTicks++; }
    public long getTotalCultivationTicks() { return totalCultivationTicks; }

    public void recordFailedMerge(long worldTime) { lastFailedMergeWorldTime = worldTime; }
    public boolean isMergeCoolingDown(long worldTime) {
        return lastFailedMergeWorldTime >= 0 && worldTime - lastFailedMergeWorldTime < MERGE_COOLDOWN_TICKS;
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("cultivation_ticks", totalCultivationTicks);
        tag.putLong("last_failed_merge", lastFailedMergeWorldTime);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(totalCultivationTicks);
        buf.writeLong(lastFailedMergeWorldTime);
    }

    public static BodyTechniqueData read(CompoundTag tag) {
        return new BodyTechniqueData(
            tag.getLong("cultivation_ticks"),
            tag.getLong("last_failed_merge")
        );
    }

    public static BodyTechniqueData decode(RegistryFriendlyByteBuf buf) {
        return new BodyTechniqueData(buf.readLong(), buf.readLong());
    }
}
