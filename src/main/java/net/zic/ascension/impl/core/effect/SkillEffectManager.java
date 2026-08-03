package net.zic.ascension.impl.core.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.effect.SkillEffectRemovalReason;
import net.zic.ascension.api.ascension.core.effect.SkillEffectStackingScope;
import net.zic.ascension.api.ascension.event.effect.SkillEffectEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
        SkillEffectInstance existing = findMatching(
                container,
                definitionId,
                definition.stackingScope(),
                sourceEntity,
                sourceSkill
        );
        if (existing != null) {
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
        return removeMatching(entity, definitionId, null, null, null, reason) > 0;
    }

    public static boolean removeInstance(
            LivingEntity entity,
            UUID instanceId,
            SkillEffectRemovalReason reason
    ) {
        if (entity == null || instanceId == null || entity.level().isClientSide()) {
            return false;
        }

        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<SkillEffectInstance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            if (!active.instanceId().equals(instanceId)) {
                continue;
            }
            removeInstance(entity, active, resolve(entity, active.definition()), reason);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static int removeMatching(
            LivingEntity entity,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill,
            SkillEffectStackingScope scope,
            SkillEffectRemovalReason reason
    ) {
        if (entity == null || entity.level().isClientSide()) {
            return 0;
        }

        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<SkillEffectInstance> iterator = container.mutableInstances().iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            if (!matchesQuery(active, definitionId, sourceEntity, sourceSkill, scope)) {
                continue;
            }
            removeInstance(entity, active, resolve(entity, active.definition()), reason);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static int consumeStacks(
            LivingEntity entity,
            UUID instanceId,
            int amount,
            SkillEffectRemovalReason removalReason
    ) {
        if (entity == null || instanceId == null || amount <= 0 || entity.level().isClientSide()) {
            return 0;
        }

        SkillEffectContainer container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<SkillEffectInstance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            SkillEffectInstance active = iterator.next();
            if (!active.instanceId().equals(instanceId)) {
                continue;
            }

            int consumed = Math.min(amount, active.stacks());
            int remaining = active.stacks() - consumed;
            if (remaining <= 0) {
                removeInstance(entity, active, resolve(entity, active.definition()), removalReason);
                iterator.remove();
            } else {
                active.setStacks(remaining);
                NeoForge.EVENT_BUS.post(new SkillEffectEvent.Updated(entity, active));
            }
            return consumed;
        }
        return 0;
    }

    public static List<SkillEffectContext> find(
            LivingEntity entity,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        if (entity == null) {
            return List.of();
        }

        List<SkillEffectContext> result = new ArrayList<>();
        for (SkillEffectInstance active : entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).instances()) {
            if (matchesQuery(active, definitionId, sourceEntity, sourceSkill, null)) {
                result.add(active);
            }
        }
        return List.copyOf(result);
    }

    public static SkillEffectContext findInstance(LivingEntity entity, UUID instanceId) {
        if (entity == null || instanceId == null) {
            return null;
        }
        for (SkillEffectInstance active : entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).instances()) {
            if (active.instanceId().equals(instanceId)) {
                return active;
            }
        }
        return null;
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

    private static SkillEffectInstance findMatching(
            SkillEffectContainer container,
            Identifier definitionId,
            SkillEffectStackingScope scope,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        if (scope == SkillEffectStackingScope.INDEPENDENT) {
            return null;
        }
        for (SkillEffectInstance active : container.mutableInstances()) {
            if (!active.definition().equals(definitionId)) {
                continue;
            }
            if (scope == SkillEffectStackingScope.DEFINITION
                    || scope == SkillEffectStackingScope.SOURCE_ENTITY
                    && Objects.equals(active.sourceEntity(), sourceEntity)
                    || scope == SkillEffectStackingScope.SOURCE_SKILL
                    && Objects.equals(active.sourceSkill(), sourceSkill)
                    || scope == SkillEffectStackingScope.SOURCE_ENTITY_AND_SKILL
                    && Objects.equals(active.sourceEntity(), sourceEntity)
                    && Objects.equals(active.sourceSkill(), sourceSkill)) {
                return active;
            }
        }
        return null;
    }

    private static boolean matchesQuery(
            SkillEffectInstance active,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill,
            SkillEffectStackingScope scope
    ) {
        if (definitionId != null && !active.definition().equals(definitionId)) {
            return false;
        }
        if (sourceEntity != null && !sourceEntity.equals(active.sourceEntity())) {
            return false;
        }
        if (sourceSkill != null && !sourceSkill.equals(active.sourceSkill())) {
            return false;
        }
        if (scope == null || scope == SkillEffectStackingScope.INDEPENDENT) {
            return true;
        }
        return switch (scope) {
            case DEFINITION -> true;
            case SOURCE_ENTITY -> sourceEntity != null && sourceEntity.equals(active.sourceEntity());
            case SOURCE_SKILL -> sourceSkill != null && sourceSkill.equals(active.sourceSkill());
            case SOURCE_ENTITY_AND_SKILL -> sourceEntity != null
                    && sourceSkill != null
                    && sourceEntity.equals(active.sourceEntity())
                    && sourceSkill.equals(active.sourceSkill());
            case INDEPENDENT -> true;
        };
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
