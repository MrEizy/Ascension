package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// extracted it from PassiveSkill so that I can use it in other places :)
public class ToggleablePassiveSkill extends PassiveSkill implements ToggleableSkill<PassiveSkill.Data> {
    protected ToggleablePassiveSkill(
            Component name,
            Component description,
            int levels,
            int baseLevelCap,
            List<Double> levelExperienceRequirements,
            List<PassiveModifier> modifiers,
            List<PassiveTrigger> triggers,
            SkillDefinitions definitions,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep,
            Optional<Identifier> stateGroup,
            List<Identifier> grantedSkills,
            List<SkillCondition> enableConditions,
            List<SkillAction> enableActions,
            List<SkillAction> disableActions
    ) {
        super(name, description, levels, baseLevelCap, levelExperienceRequirements, modifiers, triggers, definitions, enabledByDefault, upkeep, stateGroup, grantedSkills, enableConditions, enableActions, disableActions);
    }

    @Override
    public boolean isEnabled(Data data) {
        return data.isEnabled();
    }

    @Override
    public void setEnabled(Data data, boolean enabled) {
        data.setEnabled(enabled);
    }

    @Override
    public boolean canEnable(LivingEntity entity, OriginSource source, SkillInstance<Data> instance) {
        if (!upkeepAvailable(entity, source, instance.getSkillId(), true) || !(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        SkillActionContext context = new SkillActionContext(level, entity, instance.getSkillId(), null, null, 0.0D, Map.of());
        return enableConditions().stream().allMatch(condition -> condition != null && condition.test(context));
    }

    @Override
    public void onEnabled(OriginSource source, SkillInstance<Data> instance) {
        activateState(source, instance.getSkillId());
    }

    @Override
    public void onDisabled(OriginSource source, SkillInstance<Data> instance) {
        deactivateState(source, instance.getSkillId());
    }

    @Override
    public boolean tickEnabled(LivingEntity entity, OriginSource source, SkillInstance<Data> instance) {
        return getUpkeep().isEmpty()
                || entity.level().getGameTime() % getUpkeep().get().interval() != 0L
                || upkeepAvailable(entity, source, instance.getSkillId(), false);
    }

    private boolean upkeepAvailable(LivingEntity entity, OriginSource source, Identifier skillId, boolean simulate) {
        if (getUpkeep().isEmpty()) {
            return true;
        }
        Upkeep upkeep = getUpkeep().get();
        ScaledValue.Context valueContext = new ScaledValue.Context(source, skillId, entity, null, 0.0D, Map.of());
        ResourceTransactionRequest request = ResourceTransactionRequest.of(
                entity,
                upkeep.resource(),
                upkeep.operation(),
                Math.max(0.0D, upkeep.amount().resolve(valueContext)),
                new ResourceSourceIdentity.Simple(upkeep.source(), Set.of(ResourceSourceIdentity.Tags.SKILL))
        ).withSkill(skillId);
        if (simulate) {
            request = request.withFlags(Set.of(ResourceTransactionRequest.Flag.SIMULATE));
        }
        return ResourceTransactionService.transact(request).succeeded();
    }
}
