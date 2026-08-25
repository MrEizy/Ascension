package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;

public final class ActiveCastData implements CastData {
    private int ticks;
    private int stageIndex;
    private double cumulativeCostTarget;
    private double interruptionDamage;
    private int lastDamageTick = Integer.MIN_VALUE;
    private boolean dirty = true;
    private int lastSyncedTicks;

    public ActiveCastData() {
    }

    public ActiveCastData(ByteBuf buf) {
        ticks = Math.max(0, buf.readInt());
        stageIndex = Math.max(0, buf.readInt());
        cumulativeCostTarget = Math.max(0.0D, buf.readDouble());
        interruptionDamage = Math.max(0.0D, buf.readDouble());
        lastDamageTick = buf.readInt();
        lastSyncedTicks = ticks;
        dirty = false;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = Math.max(0, ticks);
        if (Math.abs(this.ticks - lastSyncedTicks) >= 5) {
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
        buf.writeInt(ticks);
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
        lastSyncedTicks = ticks;
    }
}
