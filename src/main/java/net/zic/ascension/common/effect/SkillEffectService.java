package net.zic.ascension.common.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.effect.SkillEffectContext;
import net.zic.ascension.api.core.effect.SkillEffectRemovalReason;
import net.zic.ascension.api.core.effect.SkillEffectStackingScope;
import net.zic.ascension.impl.effect.runtime.SkillEffectManager;

import java.util.List;
import java.util.UUID;

public final class SkillEffectService {
    private SkillEffectService() {
    }

    public static boolean apply(
            LivingEntity target,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill,
            int duration,
            double potency
    ) {
        return SkillEffectManager.apply(
                target,
                definitionId,
                sourceEntity,
                sourceSkill,
                duration,
                potency
        );
    }

    public static boolean remove(LivingEntity target, Identifier definitionId) {
        return SkillEffectManager.remove(target, definitionId, SkillEffectRemovalReason.MANUAL);
    }

    public static boolean removeInstance(LivingEntity target, UUID instanceId) {
        return SkillEffectManager.removeInstance(target, instanceId, SkillEffectRemovalReason.MANUAL);
    }

    public static int removeFromSource(
            LivingEntity target,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        return SkillEffectManager.removeMatching(
                target,
                definitionId,
                sourceEntity,
                sourceSkill,
                SkillEffectStackingScope.SOURCE_ENTITY_AND_SKILL,
                SkillEffectRemovalReason.MANUAL
        );
    }

    public static int consumeStacks(LivingEntity target, UUID instanceId, int amount) {
        return SkillEffectManager.consumeStacks(
                target,
                instanceId,
                amount,
                SkillEffectRemovalReason.MANUAL
        );
    }

    public static List<SkillEffectContext> find(
            LivingEntity target,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        return SkillEffectManager.find(target, definitionId, sourceEntity, sourceSkill);
    }

    public static SkillEffectContext findInstance(LivingEntity target, UUID instanceId) {
        return SkillEffectManager.findInstance(target, instanceId);
    }

    public static boolean has(
            LivingEntity target,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        return !find(target, definitionId, sourceEntity, sourceSkill).isEmpty();
    }

    public static int clear(LivingEntity target) {
        return SkillEffectManager.clear(target, SkillEffectRemovalReason.CLEARED);
    }

    public static boolean hasActiveEffects(LivingEntity target) {
        return SkillEffectManager.hasActiveEffects(target);
    }
}
