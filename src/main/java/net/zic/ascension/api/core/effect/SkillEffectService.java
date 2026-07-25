package net.zic.ascension.api.core.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import java.util.Iterator;
import java.util.UUID;

public final class SkillEffectService {
    private SkillEffectService() {
    }

    public static void apply(LivingEntity target, Identifier definitionId, UUID sourceEntity, Identifier sourceSkill, int duration, double potency) {
        SkillEffectDefinition definition = CoreRegistries.safeAccess(CoreRegistries.SKILL_EFFECT_REGISTRY, definitionId, target.registryAccess());
        if (definition == null || duration <= 0 || potency <= 0.0D) {
            return;
        }
        SkillEffectContainer container = target.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        for (SkillEffectInstance existing : container.instances()) {
            if (!existing.definition().equals(definitionId)) {
                continue;
            }
            switch (definition.stacking()) {
                case REFRESH -> existing.setRemainingDuration(Math.max(existing.remainingDuration(), duration));
                case STRONGER_REPLACES -> {
                    if (potency >= existing.potency()) {
                        existing.setPotency(potency);
                        existing.setRemainingDuration(duration);
                    } else {
                        existing.setRemainingDuration(Math.max(existing.remainingDuration(), duration));
                    }
                }
                case STACK -> {
                    existing.incrementStacks();
                    existing.setRemainingDuration(Math.max(existing.remainingDuration(), duration));
                    existing.setPotency(Math.max(existing.potency(), potency));
                }
            }
            return;
        }
        SkillEffectInstance created = new SkillEffectInstance(definitionId, sourceEntity, sourceSkill, duration, potency);
        container.instances().add(created);
        for (SkillEffectModule module : definition.modules()) {
            module.onApply(target, created);
        }
    }

    public static void tick(LivingEntity entity) {
        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<SkillEffectInstance> iterator = container.instances().iterator();
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            SkillEffectDefinition definition = CoreRegistries.safeAccess(CoreRegistries.SKILL_EFFECT_REGISTRY, active.definition(), entity.registryAccess());
            if (definition == null) {
                iterator.remove();
                continue;
            }
            for (SkillEffectModule module : definition.modules()) {
                module.tick(entity, active);
            }
            if (active.tickDuration()) {
                for (SkillEffectModule module : definition.modules()) {
                    module.onRemove(entity, active);
                }
                iterator.remove();
            }
        }
    }
}
