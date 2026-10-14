package net.zic.ascension.common.entities.npc.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;
import net.zic.ascension.common.entities.npc.NpcProfile;

import java.util.EnumSet;
import java.util.List;

public final class NpcSocialGoal extends Goal {
    private final CultivatorNpcEntity npc;
    private int cooldown;
    private int sessionTicks;
    private CultivatorNpcEntity candidate;

    public NpcSocialGoal(CultivatorNpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        cooldown = 200 + npc.getRandom().nextInt(401);
    }

    @Override
    public boolean canUse() {
        if (npc.getSocialPartner() != null) return true;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!npc.canBeginSocialEncounter()) {
            cooldown = 80;
            return false;
        }
        NpcProfile.DailyLife routine = npc.getDailyLife();
        List<CultivatorNpcEntity> neighbors = npc.level().getEntitiesOfClass(CultivatorNpcEntity.class,
                npc.getBoundingBox().inflate(routine.socialRadius()), other ->
                        other != npc && other.canBeginSocialEncounter() && npc.getSensing().hasLineOfSight(other) &&
                                npc.distanceToSqr(other) <= routine.socialRadius() * routine.socialRadius());
        if (neighbors.isEmpty()) {
            cooldown = 120;
            return false;
        }
        candidate = neighbors.get(npc.getRandom().nextInt(neighbors.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return sessionTicks > 0 && npc.canContinueSocialEncounter();
    }

    @Override
    public void start() {
        if (npc.getSocialPartner() == null) {
            if (candidate == null || !npc.startSocialEncounter(candidate)) {
                sessionTicks = 0;
                return;
            }
            npc.announceSocialEncounter(candidate);
        }
        sessionTicks = npc.getDailyLife().socialDurationTicks();
        npc.getNavigation().stop();
        candidate = null;
    }

    @Override
    public void tick() {
        CultivatorNpcEntity partner = npc.getSocialPartner();
        if (partner == null) {
            sessionTicks = 0;
            return;
        }
        npc.getNavigation().stop();
        npc.getLookControl().setLookAt(partner, 30.0F, 30.0F);
        sessionTicks--;
    }

    @Override
    public void stop() {
        npc.endSocialEncounter();
        NpcProfile.DailyLife routine = npc.getDailyLife();
        int min = routine.socialMinIntervalTicks();
        int max = routine.socialMaxIntervalTicks();
        int interval = min + npc.getRandom().nextInt(Math.max(1, max - min + 1));
        cooldown = npc.getHabits().socialCooldown(interval, npc.level().getGameTime());
        sessionTicks = 0;
        candidate = null;
    }
}
