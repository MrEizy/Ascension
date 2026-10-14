package net.zic.ascension.common.entities.npc.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;
import net.zic.ascension.common.entities.npc.NpcDailyActivity;
import net.zic.ascension.common.entities.npc.NpcProfile;

import java.util.EnumSet;

public final class NpcCultivationGoal extends Goal {
    private static final int ENTER_TICKS = 50;
    private static final int EXIT_TICKS = 40;

    private final CultivatorNpcEntity npc;
    private int cooldown;
    private int stageTicks;
    private int meditationTicks;
    private int circulationTicks;
    private boolean performCirculation;
    private boolean completed;

    public NpcCultivationGoal(CultivatorNpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        cooldown = 160 + npc.getRandom().nextInt(241);
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!npc.canBeginDailyCultivation()) {
            cooldown = 40;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !completed && npc.isCultivating() && npc.canContinueDailyCultivation();
    }

    @Override
    public void start() {
        NpcProfile.CultivationRoutine routine = npc.getDailyCultivationRoutine();
        meditationTicks = routine.meditationTicks();
        circulationTicks = routine.circulationTicks();
        performCirculation = npc.getRandom().nextInt(100) < routine.circulationChance();
        completed = false;
        npc.getNavigation().stop();
        transition(NpcDailyActivity.CULTIVATION_ENTER, ENTER_TICKS);
    }

    @Override
    public void tick() {
        npc.getNavigation().stop();
        if (--stageTicks > 0) return;
        switch (npc.getDailyActivity()) {
            case CULTIVATION_ENTER -> transition(NpcDailyActivity.MEDITATION, meditationTicks);
            case MEDITATION -> {
                if (performCirculation) transition(NpcDailyActivity.QI_CIRCULATION, circulationTicks);
                else transition(NpcDailyActivity.CULTIVATION_EXIT, EXIT_TICKS);
            }
            case QI_CIRCULATION -> transition(NpcDailyActivity.CULTIVATION_EXIT, EXIT_TICKS);
            case CULTIVATION_EXIT, NONE -> completed = true;
        }
    }

    @Override
    public void stop() {
        npc.setDailyActivity(NpcDailyActivity.NONE);
        NpcProfile.CultivationRoutine routine = npc.getDailyCultivationRoutine();
        int minimum = completed ? routine.minIntervalTicks() : Math.min(200, routine.minIntervalTicks());
        int maximum = completed ? routine.maxIntervalTicks() : Math.min(400, routine.maxIntervalTicks());
        cooldown = minimum + npc.getRandom().nextInt(Math.max(1, maximum - minimum + 1));
        completed = false;
    }

    private void transition(NpcDailyActivity activity, int duration) {
        npc.setDailyActivity(activity);
        stageTicks = Math.max(1, duration);
    }
}
