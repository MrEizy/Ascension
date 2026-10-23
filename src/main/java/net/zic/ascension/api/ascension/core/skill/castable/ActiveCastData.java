package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

/**
 * cast data of an active skill. ticks are tracked by the {@link CastingSkillInstance} phase, not here
 */
public final class ActiveCastData implements CastData {
    public static final SyncHandler<ActiveCastData> SYNC_HANDLER = new DataSyncHandler();

    private int stageIndex;
    private double cumulativeCostTarget;
    private double interruptionDamage;
    private int lastDamageTick = Integer.MIN_VALUE;

    public int getStageIndex() {
        return stageIndex;
    }

    //returns true if the stage changed
    public boolean setStageIndex(int stageIndex) {
        int resolved = Math.max(0, stageIndex);
        if (this.stageIndex == resolved) return false;
        this.stageIndex = resolved;
        return true;
    }

    public double getCumulativeCostTarget() {
        return cumulativeCostTarget;
    }

    public void setCumulativeCostTarget(double cumulativeCostTarget) {
        this.cumulativeCostTarget = Math.max(0.0D, cumulativeCostTarget);
    }

    public double addInterruptionDamage(double damage, int ticksElapsed, int window) {
        if (lastDamageTick == Integer.MIN_VALUE || ticksElapsed - lastDamageTick > window) {
            interruptionDamage = 0.0D;
        }
        interruptionDamage += Math.max(0.0D, damage);
        lastDamageTick = ticksElapsed;
        return interruptionDamage;
    }

    //only the stage is needed by the client, cost and interruption are server only
    private static final class DataSyncHandler implements SyncHandler<ActiveCastData> {
        @Override
        public void decode(ActiveCastData data, ByteBuf buf, RegistryAccess access) {
            data.stageIndex = Math.max(0, buf.readInt());
        }

        @Override
        public void encode(ActiveCastData data, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(data.stageIndex);
        }
    }
}
