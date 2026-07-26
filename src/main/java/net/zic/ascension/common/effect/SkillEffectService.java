package net.zic.ascension.common.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.impl.effect.runtime.SkillEffectManager;

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

    public static int clear(LivingEntity target) {
        return SkillEffectManager.clear(target, SkillEffectRemovalReason.CLEARED);
    }

    public static boolean hasActiveEffects(LivingEntity target) {
        return SkillEffectManager.hasActiveEffects(target);
    }
}
