package net.zic.ascension.impl.effect.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.core.effect.SkillEffectModule;
import net.zic.ascension.api.core.effect.SkillEffectRemovalReason;
import net.zic.ascension.api.event.effect.SkillEffectEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import java.util.Iterator;
import java.util.UUID;

public final class SkillEffectManager {
    private SkillEffectManager() {
    }

    public static boolean apply(
            LivingEntity target,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill,
            int duration,
            double potency
    ) {
        if (target == null
                || definitionId == null
                || duration <= 0
                || !Double.isFinite(potency)
                || potency <= 0.0D
                || target.level().isClientSide()) {
            return false;
        }

        SkillEffectDefinition definition = resolve(target, definitionId);
        if (definition == null || definition.modules().isEmpty()) {
            return false;
        }

        SkillEffectEvent.Apply event = new SkillEffectEvent.Apply(
                target,
                definitionId,
                sourceEntity,
                sourceSkill,
                duration,
                potency
        );
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return false;
        }

        SkillEffectContainer container = target.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        for (SkillEffectInstance existing : container.mutableInstances()) {
            if (!existing.definition().equals(definitionId)) {
                continue;
            }

            updateExisting(existing, definition, duration, potency);
            for (SkillEffectModule module : definition.modules()) {
                module.onUpdate(target, existing);
            }
            NeoForge.EVENT_BUS.post(new SkillEffectEvent.Updated(target, existing));
            return true;
        }

        SkillEffectInstance created = new SkillEffectInstance(
                definitionId,
                sourceEntity,
                sourceSkill,
                duration,
                potency
        );
        container.mutableInstances().add(created);
        for (SkillEffectModule module : definition.modules()) {
            module.onApply(target, created);
        }
        return true;
    }

    public static boolean remove(
            LivingEntity entity,
            Identifier definitionId,
            SkillEffectRemovalReason reason
    ) {
        if (entity == null || definitionId == null || entity.level().isClientSide()) {
            return false;
        }

        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<SkillEffectInstance> iterator = container.mutableInstances().iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            if (!active.definition().equals(definitionId)) {
                continue;
            }
            removeInstance(entity, active, resolve(entity, active.definition()), reason);
            iterator.remove();
            removed = true;
        }
        return removed;
    }

    public static int clear(LivingEntity entity, SkillEffectRemovalReason reason) {
        if (entity == null || entity.level().isClientSide()) {
            return 0;
        }

        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        int removed = container.mutableInstances().size();
        for (SkillEffectInstance active : container.mutableInstances()) {
            removeInstance(entity, active, resolve(entity, active.definition()), reason);
        }
        container.mutableInstances().clear();
        return removed;
    }

    public static void tick(LivingEntity entity) {
        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        if (container.isEmpty()) {
            return;
        }

        Iterator<SkillEffectInstance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            SkillEffectDefinition definition = resolve(entity, active.definition());
            if (definition == null || definition.modules().isEmpty()) {
                removeInstance(entity, active, definition, SkillEffectRemovalReason.MISSING_DEFINITION);
                iterator.remove();
                continue;
            }

            boolean conditionRemoval = false;
            for (SkillEffectModule module : definition.modules()) {
                module.tick(entity, active);
                conditionRemoval |= module.shouldRemove(entity, active);
            }

            if (conditionRemoval) {
                removeInstance(entity, active, definition, SkillEffectRemovalReason.CONDITION);
                iterator.remove();
                continue;
            }

            if (active.tickDuration()) {
                removeInstance(entity, active, definition, SkillEffectRemovalReason.EXPIRED);
                iterator.remove();
            }
        }
    }

    public static boolean hasActiveEffects(LivingEntity entity) {
        return entity != null
                && !entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).isEmpty();
    }

    private static SkillEffectDefinition resolve(LivingEntity entity, Identifier definitionId) {
        return CoreRegistries.safeAccess(
                CoreRegistries.SKILL_EFFECT_REGISTRY,
                definitionId,
                entity.registryAccess()
        );
    }

    private static void updateExisting(
            SkillEffectInstance existing,
            SkillEffectDefinition definition,
            int duration,
            double potency
    ) {
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
                existing.setStacks(Math.min(definition.maxStacks(), existing.stacks() + 1));
                existing.setRemainingDuration(Math.max(existing.remainingDuration(), duration));
                existing.setPotency(Math.max(existing.potency(), potency));
            }
        }
    }

    private static void removeInstance(
            LivingEntity entity,
            SkillEffectInstance active,
            SkillEffectDefinition definition,
            SkillEffectRemovalReason reason
    ) {
        if (definition != null) {
            for (SkillEffectModule module : definition.modules()) {
                module.onRemove(entity, active);
            }
        }
        NeoForge.EVENT_BUS.post(new SkillEffectEvent.Removed(entity, active, reason));
    }
}
