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
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;

import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.impl.core.targeting.TargetingService;
import net.zic.ascension.impl.runtime.visual.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AreaFields {
    private static final List<AreaFieldInstance> FIELDS = new ArrayList<>();

    private AreaFields() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId,
            Vec3 center
    ) {
        AreaFieldDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.AREA_FIELD_REGISTRY,
                definitionId,
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
        AreaFieldInstance field = new AreaFieldInstance(
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
        definition.visual().ifPresent(visual -> {
            double radius = definition.radius().resolve(context.scaledValueContext());
            double height = definition.height().resolve(context.scaledValueContext());
            RuntimeVisualSync.spawn(
                    context.level(),
                    visualState(field, visual, field.expiresAt(), radius, height)
            );
        });
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
        Iterator<AreaFieldInstance> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            AreaFieldInstance field = iterator.next();
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
        for (AreaFieldInstance field : List.copyOf(FIELDS)) {
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

    private static boolean tick(ServerLevel level, AreaFieldInstance field) {
        Entity ownerEntity = level.getEntity(field.ownerId());
        if (!(ownerEntity instanceof ServerPlayer owner) || owner.isRemoved()) {
            return false;
        }

        AreaFieldDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.AREA_FIELD_REGISTRY,
                field.definitionId(),
                level.registryAccess()
        );
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
        Iterator<AreaFieldInstance> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            AreaFieldInstance field = iterator.next();
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

    private static void applyExpiry(ServerLevel level, AreaFieldInstance field) {
        Entity ownerEntity = level.getEntity(field.ownerId());
        if (!(ownerEntity instanceof ServerPlayer owner)) {
            return;
        }
        AreaFieldDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.AREA_FIELD_REGISTRY,
                field.definitionId(),
                level.registryAccess()
        );
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

    private static void removeVisual(ServerLevel level, AreaFieldInstance field) {
        AreaFieldDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.AREA_FIELD_REGISTRY,
                field.definitionId(),
                level.registryAccess()
        );
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
        RuntimeVisualSync.remove(
                level,
                visualState(field, definition.visual().get(), 0L, radius, height)
        );
    }

    private static RuntimeVisualState visualState(
            AreaFieldInstance field,
            Identifier visual,
            long expiresAt,
            double radius,
            double height
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
                height
        );
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
            AreaFieldInstance field,
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
}
