package net.zic.ascension.impl.core.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;

import java.util.List;
import java.util.UUID;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;

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
        return SkillEffectManager.remove(target, definitionId, SkillEffectDefinition.RemovalReason.MANUAL);
    }

    public static boolean removeInstance(LivingEntity target, UUID instanceId) {
        return SkillEffectManager.removeInstance(target, instanceId, SkillEffectDefinition.RemovalReason.MANUAL);
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
                SkillEffectDefinition.Scope.SOURCE_ENTITY_AND_SKILL,
                SkillEffectDefinition.RemovalReason.MANUAL
        );
    }

    public static int consumeStacks(LivingEntity target, UUID instanceId, int amount) {
        return SkillEffectManager.consumeStacks(
                target,
                instanceId,
                amount,
                SkillEffectDefinition.RemovalReason.MANUAL
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
        return SkillEffectManager.clear(target, SkillEffectDefinition.RemovalReason.CLEARED);
    }

    public static boolean hasActiveEffects(LivingEntity target) {
        return SkillEffectManager.hasActiveEffects(target);
    }
}
