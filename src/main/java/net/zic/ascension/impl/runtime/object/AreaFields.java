package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;

import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.impl.core.targeting.TargetingService;
import net.zic.ascension.impl.runtime.object.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Map;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AreaFields {
    private static final List<Instance> FIELDS = new ArrayList<>();

    private AreaFields() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId,
            Vec3 center
    ) {
        AreaFieldDefinition definition = SkillDefinitions.resolveStored(
                AreaFieldDefinition.class,
                context.skill(),
                definitionId,
                CoreRegistries.AREA_FIELD_REGISTRY,
                context.level().registryAccess()
        );
        if (definition == null) {
            return null;
        }

        double durationValue = definition.duration().resolve(context.scaledValueContext());
        if (!Double.isFinite(durationValue) || durationValue <= 0.0D) {
            return null;
        }

        long createdAt = context.level().getGameTime();
        Instance field = new Instance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                center == null ? context.position() : center,
                context.charge(),
                context.variables(),
                createdAt,
                createdAt + Math.max(1L, Math.round(durationValue))
        );
        FIELDS.add(field);
        Resolved<RuntimeVisualDefinition> visual = visual(context, definition.visual());
        if (visual != null) {
            double radius = definition.radius().resolve(context.scaledValueContext());
            double height = definition.height().resolve(context.scaledValueContext());
            RuntimeVisualSync.spawn(
                    context.level(),
                    visualState(field, visual.id(), field.expiresAt(), radius, height, visual.value())
            );
        }
        return field.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        return runtimeId != null && FIELDS.removeIf(field -> field.runtimeId().equals(runtimeId));
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        return finish(level, runtimeId, false);
    }

    public static boolean expire(ServerLevel level, UUID runtimeId) {
        return finish(level, runtimeId, true);
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int before = FIELDS.size();
        FIELDS.removeIf(field -> field.ownerId().equals(ownerId)
                && (definitionId == null || field.definitionId().equals(definitionId)));
        return before - FIELDS.size();
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        if (level == null || ownerId == null) {
            return 0;
        }
        int removed = 0;
        Iterator<Instance> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            Instance field = iterator.next();
            if (!field.ownerId().equals(ownerId)
                    || definitionId != null && !field.definitionId().equals(definitionId)) {
                continue;
            }
            removeVisual(level, field);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Instance field : List.copyOf(FIELDS)) {
            if (!FIELDS.contains(field)) {
                continue;
            }
            ServerLevel level = event.getServer().getLevel(field.dimension());
            if (level == null || !tick(level, field)) {
                if (level != null) {
                    removeVisual(level, field);
                }
                FIELDS.remove(field);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        FIELDS.clear();
    }

    private static boolean tick(ServerLevel level, Instance field) {
        Entity ownerEntity = level.getEntity(field.ownerId());
        if (!(ownerEntity instanceof ServerPlayer owner) || owner.isRemoved()) {
            return false;
        }

        AreaFieldDefinition definition = SkillDefinitions.resolveStored(AreaFieldDefinition.class, field.skillId(), field.definitionId(), CoreRegistries.AREA_FIELD_REGISTRY, level.registryAccess());
        if (definition == null) {
            return false;
        }

        SkillExecutionContext baseContext = new SkillExecutionContext(
                level,
                owner,
                field.skillId(),
                null,
                field.center(),
                field.charge(),
                field.variables()
        );
        double radius = definition.radius().resolve(baseContext.scaledValueContext());
        double height = definition.height().resolve(baseContext.scaledValueContext());
        if (!Double.isFinite(radius) || radius <= 0.0D || !Double.isFinite(height) || height <= 0.0D) {
            return false;
        }

        Set<UUID> current = new HashSet<>();
        AABB bounds = definition.shape() == AreaFieldDefinition.Shape.SPHERE
                ? new AABB(field.center(), field.center()).inflate(radius)
                : new AABB(
                        field.center().x - radius,
                        field.center().y - height * 0.5D,
                        field.center().z - radius,
                        field.center().x + radius,
                        field.center().y + height * 0.5D,
                        field.center().z + radius
                );

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                entity -> TargetingService.matches(owner, entity, definition.filter())
                        && contains(definition.shape(), field.center(), radius, height, entity)
        )) {
            current.add(target.getUUID());
            if (!field.inside().contains(target.getUUID())) {
                applyFeatures(level, owner, field, target, definition.onEnter());
            }
        }

        for (UUID previous : new HashSet<>(field.inside())) {
            if (current.contains(previous)) {
                continue;
            }
            Entity entity = level.getEntity(previous);
            if (entity instanceof LivingEntity target) {
                applyFeatures(level, owner, field, target, definition.onExit());
            }
        }

        field.inside().clear();
        field.inside().addAll(current);

        if ((level.getGameTime() - field.createdAt()) % definition.tickInterval() == 0L) {
            for (UUID id : current) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity target) {
                    applyFeatures(level, owner, field, target, definition.onTick());
                }
            }
        }

        if (level.getGameTime() >= field.expiresAt()) {
            for (UUID id : current) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity target) {
                    applyFeatures(level, owner, field, target, definition.onExit());
                    applyFeatures(level, owner, field, target, definition.onExpire());
                }
            }
            return false;
        }
        return true;
    }


    private static boolean finish(ServerLevel level, UUID runtimeId, boolean expire) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<Instance> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            Instance field = iterator.next();
            if (!field.runtimeId().equals(runtimeId)) {
                continue;
            }
            if (expire) {
                applyExpiry(level, field);
            }
            removeVisual(level, field);
            iterator.remove();
            return true;
        }
        return false;
    }

    private static void applyExpiry(ServerLevel level, Instance field) {
        Entity ownerEntity = level.getEntity(field.ownerId());
        if (!(ownerEntity instanceof ServerPlayer owner)) {
            return;
        }
        AreaFieldDefinition definition = SkillDefinitions.resolveStored(AreaFieldDefinition.class, field.skillId(), field.definitionId(), CoreRegistries.AREA_FIELD_REGISTRY, level.registryAccess());
        if (definition == null) {
            return;
        }
        for (UUID id : Set.copyOf(field.inside())) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity target) {
                applyFeatures(level, owner, field, target, definition.onExit());
                applyFeatures(level, owner, field, target, definition.onExpire());
            }
        }
    }

    private static void removeVisual(ServerLevel level, Instance field) {
        AreaFieldDefinition definition = SkillDefinitions.resolveStored(AreaFieldDefinition.class, field.skillId(), field.definitionId(), CoreRegistries.AREA_FIELD_REGISTRY, level.registryAccess());
        if (definition == null || definition.visual().isEmpty()) {
            return;
        }
        Entity ownerEntity = level.getEntity(field.ownerId());
        LivingEntity target = ownerEntity instanceof LivingEntity living ? living : null;
        SkillExecutionContext context = target instanceof ServerPlayer owner
                ? new SkillExecutionContext(
                        level,
                        owner,
                        field.skillId(),
                        null,
                        field.center(),
                        field.charge(),
                        field.variables()
                )
                : null;
        double radius = context == null ? 0.0D : definition.radius().resolve(context.scaledValueContext());
        double height = context == null ? 0.0D : definition.height().resolve(context.scaledValueContext());
        Resolved<RuntimeVisualDefinition> visual = context == null
                ? null
                : visual(context, definition.visual());
        if (visual != null) {
            RuntimeVisualSync.remove(
                    level,
                    visualState(field, visual.id(), 0L, radius, height, visual.value())
            );
        }
    }

    private static RuntimeVisualState visualState(
            Instance field,
            Identifier visual,
            long expiresAt,
            double radius,
            double height,
            RuntimeVisualDefinition definition
    ) {
        return new RuntimeVisualState(
                field.runtimeId(),
                visual,
                field.ownerId(),
                field.center(),
                Vec3.ZERO,
                List.of(),
                List.of(),
                expiresAt,
                0,
                0,
                0.0F,
                field.runtimeId().getMostSignificantBits(),
                radius,
                height,
                definition
        );
    }

    private static Resolved<RuntimeVisualDefinition> visual(
            SkillExecutionContext context,
            java.util.Optional<DefinitionRef<RuntimeVisualDefinition>> reference
    ) {
        return reference.map(value -> SkillDefinitions.visual(context, value)).orElse(null);
    }

    private static boolean contains(
            AreaFieldDefinition.Shape shape,
            Vec3 center,
            double radius,
            double height,
            LivingEntity entity
    ) {
        Vec3 position = entity.getBoundingBox().getCenter();
        if (shape == AreaFieldDefinition.Shape.SPHERE) {
            return position.distanceToSqr(center) <= radius * radius;
        }
        double horizontal = new Vec3(position.x - center.x, 0.0D, position.z - center.z).lengthSqr();
        return horizontal <= radius * radius && Math.abs(position.y - center.y) <= height * 0.5D;
    }

    private static void applyFeatures(
            ServerLevel level,
            ServerPlayer owner,
            Instance field,
            LivingEntity target,
            List<SkillExecutionFeature> features
    ) {
        SkillExecutionContext context = new SkillExecutionContext(
                level,
                owner,
                field.skillId(),
                target,
                target.getBoundingBox().getCenter(),
                field.charge(),
                field.variables()
        );
        for (SkillExecutionFeature feature : features) {
            feature.apply(context);
        }
    }

    private static final class Instance {
        private final UUID runtimeId;
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Identifier skillId;
        private final Identifier definitionId;
        private final Vec3 center;
        private final double charge;
        private final Map<Identifier, Double> variables;
        private final long createdAt;
        private final long expiresAt;
        private final Set<UUID> inside = new HashSet<>();

        private Instance(
                ResourceKey<Level> dimension,
                UUID ownerId,
                Identifier skillId,
                Identifier definitionId,
                Vec3 center,
                double charge,
                Map<Identifier, Double> variables,
                long createdAt,
                long expiresAt
        ) {
            this.runtimeId = UUID.randomUUID();
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.skillId = skillId;
            this.definitionId = definitionId;
            this.center = center;
            this.charge = charge;
            this.variables = variables == null ? Map.of() : Map.copyOf(variables);
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
        }

        public UUID runtimeId() {
            return runtimeId;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        public UUID ownerId() {
            return ownerId;
        }

        public Identifier skillId() {
            return skillId;
        }

        public Identifier definitionId() {
            return definitionId;
        }

        public Vec3 center() {
            return center;
        }

        public double charge() {
            return charge;
        }

        public Map<Identifier, Double> variables() {
            return variables;
        }

        public long createdAt() {
            return createdAt;
        }

        public long expiresAt() {
            return expiresAt;
        }

        public Set<UUID> inside() {
            return inside;
        }
    }
}
