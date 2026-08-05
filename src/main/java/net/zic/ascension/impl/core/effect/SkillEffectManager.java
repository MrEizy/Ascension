package net.zic.ascension.impl.core.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.event.effect.SkillEffectEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.mojang.serialization.Codec;
import java.util.Collections;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

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

        SkillEffectDefinition definition = resolve(target, definitionId, sourceSkill);
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

        Container container = target.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Instance existing = findMatching(
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

        Instance created = new Instance(
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
            SkillEffectDefinition.RemovalReason reason
    ) {
        return removeMatching(entity, definitionId, null, null, null, reason) > 0;
    }

    public static boolean removeInstance(
            LivingEntity entity,
            UUID instanceId,
            SkillEffectDefinition.RemovalReason reason
    ) {
        if (entity == null || instanceId == null || entity.level().isClientSide()) {
            return false;
        }

        Container container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<Instance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            Instance active = iterator.next();
            if (!active.instanceId().equals(instanceId)) {
                continue;
            }
            removeInstance(entity, active, resolve(entity, active.definition(), active.sourceSkill()), reason);
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
            SkillEffectDefinition.Scope scope,
            SkillEffectDefinition.RemovalReason reason
    ) {
        if (entity == null || entity.level().isClientSide()) {
            return 0;
        }

        Container container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<Instance> iterator = container.mutableInstances().iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            Instance active = iterator.next();
            if (!matchesQuery(active, definitionId, sourceEntity, sourceSkill, scope)) {
                continue;
            }
            removeInstance(entity, active, resolve(entity, active.definition(), active.sourceSkill()), reason);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static int consumeStacks(
            LivingEntity entity,
            UUID instanceId,
            int amount,
            SkillEffectDefinition.RemovalReason removalReason
    ) {
        if (entity == null || instanceId == null || amount <= 0 || entity.level().isClientSide()) {
            return 0;
        }

        Container container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        Iterator<Instance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            Instance active = iterator.next();
            if (!active.instanceId().equals(instanceId)) {
                continue;
            }

            int consumed = Math.min(amount, active.stacks());
            int remaining = active.stacks() - consumed;
            if (remaining <= 0) {
                removeInstance(entity, active, resolve(entity, active.definition(), active.sourceSkill()), removalReason);
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
        for (Instance active : entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).instances()) {
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
        for (Instance active : entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).instances()) {
            if (active.instanceId().equals(instanceId)) {
                return active;
            }
        }
        return null;
    }

    public static int clear(LivingEntity entity, SkillEffectDefinition.RemovalReason reason) {
        if (entity == null || entity.level().isClientSide()) {
            return 0;
        }

        Container container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        int removed = container.mutableInstances().size();
        for (Instance active : container.mutableInstances()) {
            removeInstance(entity, active, resolve(entity, active.definition(), active.sourceSkill()), reason);
        }
        container.mutableInstances().clear();
        return removed;
    }

    public static void tick(LivingEntity entity) {
        Container container = entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS);
        if (container.isEmpty()) {
            return;
        }

        Iterator<Instance> iterator = container.mutableInstances().iterator();
        while (iterator.hasNext()) {
            Instance active = iterator.next();
            SkillEffectDefinition definition = resolve(entity, active.definition(), active.sourceSkill());
            if (definition == null || definition.modules().isEmpty()) {
                removeInstance(entity, active, definition, SkillEffectDefinition.RemovalReason.MISSING_DEFINITION);
                iterator.remove();
                continue;
            }

            boolean conditionRemoval = false;
            for (SkillEffectModule module : definition.modules()) {
                module.tick(entity, active);
                conditionRemoval |= module.shouldRemove(entity, active);
            }

            if (conditionRemoval) {
                removeInstance(entity, active, definition, SkillEffectDefinition.RemovalReason.CONDITION);
                iterator.remove();
                continue;
            }

            if (active.tickDuration()) {
                removeInstance(entity, active, definition, SkillEffectDefinition.RemovalReason.EXPIRED);
                iterator.remove();
            }
        }
    }

    public static boolean hasActiveEffects(LivingEntity entity) {
        return entity != null
                && !entity.getData(AscensionAttachments.ACTIVE_SKILL_EFFECTS).isEmpty();
    }

    private static SkillEffectDefinition resolve(
            LivingEntity entity,
            Identifier definitionId,
            Identifier sourceSkill
    ) {
        return SkillDefinitions.resolveStored(
                SkillEffectDefinition.class,
                sourceSkill,
                definitionId,
                CoreRegistries.SKILL_EFFECT_REGISTRY,
                entity.registryAccess()
        );
    }

    private static Instance findMatching(
            Container container,
            Identifier definitionId,
            SkillEffectDefinition.Scope scope,
            UUID sourceEntity,
            Identifier sourceSkill
    ) {
        if (scope == SkillEffectDefinition.Scope.INDEPENDENT) {
            return null;
        }
        for (Instance active : container.mutableInstances()) {
            if (!active.definition().equals(definitionId)) {
                continue;
            }
            if (scope == SkillEffectDefinition.Scope.DEFINITION
                    || scope == SkillEffectDefinition.Scope.SOURCE_ENTITY
                    && Objects.equals(active.sourceEntity(), sourceEntity)
                    || scope == SkillEffectDefinition.Scope.SOURCE_SKILL
                    && Objects.equals(active.sourceSkill(), sourceSkill)
                    || scope == SkillEffectDefinition.Scope.SOURCE_ENTITY_AND_SKILL
                    && Objects.equals(active.sourceEntity(), sourceEntity)
                    && Objects.equals(active.sourceSkill(), sourceSkill)) {
                return active;
            }
        }
        return null;
    }

    private static boolean matchesQuery(
            Instance active,
            Identifier definitionId,
            UUID sourceEntity,
            Identifier sourceSkill,
            SkillEffectDefinition.Scope scope
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
        if (scope == null || scope == SkillEffectDefinition.Scope.INDEPENDENT) {
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
            Instance existing,
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
            Instance active,
            SkillEffectDefinition definition,
            SkillEffectDefinition.RemovalReason reason
    ) {
        if (definition != null) {
            for (SkillEffectModule module : definition.modules()) {
                module.onRemove(entity, active);
            }
        }
        NeoForge.EVENT_BUS.post(new SkillEffectEvent.Removed(entity, active, reason));
    }

    public static final class Container {
        public static final Codec<Container> CODEC = Instance.CODEC.listOf().xmap(
                Container::new,
                Container::instances
        );

        private final List<Instance> instances;

        public Container() {
            this.instances = new ArrayList<>();
        }

        private Container(List<Instance> instances) {
            this.instances = new ArrayList<>(instances == null ? List.of() : instances);
        }

        public List<Instance> instances() {
            return Collections.unmodifiableList(instances);
        }

        List<Instance> mutableInstances() {
            return instances;
        }

        public boolean isEmpty() {
            return instances.isEmpty();
        }
    }

    public static final class Instance implements SkillEffectContext {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("definition").forGetter(Instance::definition),
                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("instance_id").forGetter(Instance::instanceId),
                Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("source_entity")
                        .forGetter(value -> Optional.ofNullable(value.sourceEntity)),
                Identifier.CODEC.optionalFieldOf("source_skill")
                        .forGetter(value -> Optional.ofNullable(value.sourceSkill)),
                Codec.INT.fieldOf("remaining_duration").forGetter(Instance::remainingDuration),
                Codec.DOUBLE.fieldOf("potency").forGetter(Instance::potency),
                Codec.INT.optionalFieldOf("stacks", 1).forGetter(Instance::stacks)
        ).apply(instance, (definition, id, sourceEntity, sourceSkill, duration, potency, stacks) ->
                new Instance(
                        definition,
                        id,
                        sourceEntity.orElse(null),
                        sourceSkill.orElse(null),
                        duration,
                        potency,
                        stacks
                )));

        private final Identifier definition;
        private final UUID instanceId;
        private final UUID sourceEntity;
        private final Identifier sourceSkill;
        private int remainingDuration;
        private double potency;
        private int stacks;

        public Instance(
                Identifier definition,
                UUID sourceEntity,
                Identifier sourceSkill,
                int duration,
                double potency
        ) {
            this(definition, UUID.randomUUID(), sourceEntity, sourceSkill, duration, potency, 1);
        }

        private Instance(
                Identifier definition,
                UUID instanceId,
                UUID sourceEntity,
                Identifier sourceSkill,
                int duration,
                double potency,
                int stacks
        ) {
            this.definition = definition;
            this.instanceId = instanceId;
            this.sourceEntity = sourceEntity;
            this.sourceSkill = sourceSkill;
            this.remainingDuration = Math.max(0, duration);
            this.potency = Math.max(0.0D, potency);
            this.stacks = Math.max(1, stacks);
        }

        @Override
        public Identifier definition() {
            return definition;
        }

        @Override
        public UUID instanceId() {
            return instanceId;
        }

        @Override
        public UUID sourceEntity() {
            return sourceEntity;
        }

        @Override
        public Identifier sourceSkill() {
            return sourceSkill;
        }

        @Override
        public int remainingDuration() {
            return remainingDuration;
        }

        @Override
        public double potency() {
            return potency;
        }

        @Override
        public int stacks() {
            return stacks;
        }

        void setRemainingDuration(int value) {
            remainingDuration = Math.max(0, value);
        }

        void setPotency(double value) {
            potency = Math.max(0.0D, value);
        }

        void setStacks(int value) {
            stacks = Math.max(1, value);
        }

        boolean tickDuration() {
            if (remainingDuration <= 0) {
                return true;
            }
            remainingDuration--;
            return remainingDuration <= 0;
        }
    }
}
