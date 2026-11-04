package net.zic.ascension.common.entities.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;
import org.jetbrains.annotations.Nullable;

public final class NpcHomeWanderGoal extends WaterAvoidingRandomStrollGoal {
    private final CultivatorNpcEntity npc;

    public NpcHomeWanderGoal(CultivatorNpcEntity npc, double speed) {
        super(npc, speed);
        this.npc = npc;
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        BlockPos home = npc.getHomePos();
        if (home == null || npc.isSummonedGuard()) return super.getPosition();
        if (npc.distanceToSqr(Vec3.atCenterOf(home)) > (double) npc.getHomeRadius() * npc.getHomeRadius())
            return Vec3.atBottomCenterOf(home);
        for (int attempt = 0; attempt < 8; attempt++) {
            Vec3 next = super.getPosition();
            if (next != null && next.distanceToSqr(Vec3.atCenterOf(home)) <=
                    (double) npc.getHomeRadius() * npc.getHomeRadius()) return next;
        }
        return null;
    }
}
