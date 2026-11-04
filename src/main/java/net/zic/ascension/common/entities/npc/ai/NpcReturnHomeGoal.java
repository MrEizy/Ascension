package net.zic.ascension.common.entities.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;

import java.util.EnumSet;

public final class NpcReturnHomeGoal extends Goal {
    private final CultivatorNpcEntity npc;
    private int retryTicks;

    public NpcReturnHomeGoal(CultivatorNpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos home = npc.getHomePos();
        if (home == null || !npc.canReturnHome()) return false;
        double limit = npc.getHomeRadius() + 6.0;
        return npc.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > limit * limit;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos home = npc.getHomePos();
        if (home == null || !npc.canReturnHome()) return false;
        double limit = npc.getHomeRadius() * 0.65;
        return npc.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > limit * limit;
    }

    @Override
    public void start() {
        retryTicks = 0;
        travelHome();
    }

    @Override
    public void tick() {
        if (--retryTicks <= 0 && npc.getNavigation().isDone()) travelHome();
    }

    private void travelHome() {
        BlockPos home = npc.getHomePos();
        if (home == null) return;
        npc.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.95);
        retryTicks = 40;
    }

    @Override
    public void stop() { npc.getNavigation().stop(); }
}
