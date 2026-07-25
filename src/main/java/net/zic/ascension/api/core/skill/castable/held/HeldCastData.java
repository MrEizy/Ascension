package net.zic.ascension.api.core.skill.castable.held;

import io.netty.buffer.ByteBuf;
import net.zic.ascension.api.core.skill.castable.CastData;

public final class HeldCastData implements CastData {
    private int chargeTicks;
    private int stageIndex;
    private double cumulativeCostTarget;
    private double interruptionDamage;
    private int lastDamageTick = Integer.MIN_VALUE;
    private boolean dirty = true;
    private int lastSyncedChargeTicks;

    public HeldCastData() {
    }

    public HeldCastData(ByteBuf buf) {
        chargeTicks = Math.max(0, buf.readInt());
        stageIndex = Math.max(0, buf.readInt());
        cumulativeCostTarget = Math.max(0.0D, buf.readDouble());
        interruptionDamage = Math.max(0.0D, buf.readDouble());
        lastDamageTick = buf.readInt();
        lastSyncedChargeTicks = chargeTicks;
        dirty = false;
    }

    public int getChargeTicks() {
        return chargeTicks;
    }

    public void setChargeTicks(int chargeTicks) {
        this.chargeTicks = Math.max(0, chargeTicks);
        if (Math.abs(this.chargeTicks - lastSyncedChargeTicks) >= 5) {
            dirty = true;
        }
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(int stageIndex) {
        int resolved = Math.max(0, stageIndex);
        if (this.stageIndex != resolved) {
            this.stageIndex = resolved;
            dirty = true;
        }
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

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(chargeTicks);
        buf.writeInt(stageIndex);
        buf.writeDouble(cumulativeCostTarget);
        buf.writeDouble(interruptionDamage);
        buf.writeInt(lastDamageTick);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void resolveDirty() {
        dirty = false;
        lastSyncedChargeTicks = chargeTicks;
    }
}
