package net.zic.ascension.skill_passives;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkills;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class ToggleableSkillTicker {
    private static final long REFRESH_INTERVAL = 600L;
    private static final long MILESTONE_INTERVAL = 100L;
    private static final Map<OriginSource, SourceState> SOURCES = new WeakHashMap<>();

    private ToggleableSkillTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (holder == null || holder.getData() == null) {
            return;
        }
        OriginSource source = holder.getData().getSource();
        if (source == null) {
            return;
        }
        long time = entity.level().getGameTime();
        SourceState state = SOURCES.computeIfAbsent(source, unused -> new SourceState());
        if (state.lastTick == time) {
            return;
        }
        state.lastTick = time;
        if (time >= state.nextRefresh || time < state.lastRefresh) {
            refresh(source, state, time);
        }

        for (Identifier skillId : List.copyOf(state.toggleableIds)) {
            SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
            if (instance == null || !ToggleableSkills.isEnabled(instance)) {
                continue;
            }
            if (!ToggleableSkills.tick(entity, source, instance)
                    && ToggleableSkills.disable(source, instance, List.of(entity))) {
                AscensionOriginSourceHelper.markSkillDirty(source, skillId);
            }
        }

        if (time >= state.nextMilestoneCheck || time < state.lastMilestoneCheck) {
            state.lastMilestoneCheck = time;
            state.nextMilestoneCheck = time + MILESTONE_INTERVAL;
            for (Identifier skillId : List.copyOf(state.milestoneIds)) {
                checkMilestones(source, skillId);
            }
        }
    }

    @SubscribeEvent
    public static void onSkillAdded(SkillEvent.Added.Post event) {
        invalidate(event.getSource());
    }

    @SubscribeEvent
    public static void onSkillRemoved(SkillEvent.Removed.Post event) {
        invalidate(event.getSource());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SOURCES.clear();
    }

    private static void invalidate(OriginSource source) {
        SourceState state = SOURCES.get(source);
        if (state != null) {
            state.nextRefresh = Long.MIN_VALUE;
        }
    }

    private static void refresh(OriginSource source, SourceState state, long time) {
        state.toggleableIds.clear();
        state.milestoneIds.clear();
        for (SkillInstance<?> instance : List.copyOf(AscensionOriginSourceHelper.getSkillInstances(source))) {
            if (ToggleableSkills.isToggleable(instance)) {
                state.toggleableIds.add(instance.getSkillId());
            }
            if (instance.getSkill() instanceof PassiveSkill passive && !passive.training().milestones().isEmpty()) {
                state.milestoneIds.add(instance.getSkillId());
            }
        }
        state.lastRefresh = time;
        state.nextRefresh = time + REFRESH_INTERVAL;
    }

    private static void checkMilestones(OriginSource source, Identifier skillId) {
        SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
        if (instance == null || !(instance.getSkill() instanceof PassiveSkill passive)
                || !(instance.getData() instanceof PassiveSkill.Data data)) {
            return;
        }
        SkillProgressionData progression = data.getSkillProgression();
        int current = progression.getTrainedProgression();
        int cap = progression.getAccessibleCap(passive.getDefaultProgressionCap(), passive.getMaximumProgression());
        int candidate = current;
        while (candidate < cap) {
            int next = candidate + 1;
            PassiveSkill.Milestone milestone = passive.training().milestones().stream()
                    .filter(entry -> entry.level() == next).findFirst().orElse(null);
            if (milestone == null || milestone.requirements().requirements().isEmpty()
                    || !milestone.requirements().test(source)) {
                break;
            }
            candidate = next;
        }
        if (candidate != current) {
            SkillProgressionService.setTrainedProgression(source, skillId, candidate);
        }
    }

    private static final class SourceState {
        private long lastTick = Long.MIN_VALUE;
        private long lastRefresh = Long.MIN_VALUE;
        private long nextRefresh = Long.MIN_VALUE;
        private long lastMilestoneCheck = Long.MIN_VALUE;
        private long nextMilestoneCheck = Long.MIN_VALUE;
        private final List<Identifier> toggleableIds = new ArrayList<>();
        private final List<Identifier> milestoneIds = new ArrayList<>();
    }
}
