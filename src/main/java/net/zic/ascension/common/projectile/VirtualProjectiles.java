package net.zic.ascension.common.projectile;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.client.visual.RuntimeVisualState;
import net.zic.ascension.api.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.core.projectile.ProjectileBehaviorContext;
import net.zic.ascension.api.core.projectile.ProjectileLaunchDirection;
import net.zic.ascension.api.core.projectile.VirtualProjectileDefinition;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.common.visual.RuntimeVisualSync;
import net.zic.ascension.impl.core.projectile.VirtualProjectileInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class VirtualProjectiles {
    private static final List<VirtualProjectileInstance> PROJECTILES = new ArrayList<>();

    private VirtualProjectiles() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId,
            ProjectileLaunchDirection launchDirection
    ) {
        VirtualProjectileDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.VIRTUAL_PROJECTILE_REGISTRY,
                definitionId,
                context.level().registryAccess()
        );
        if (definition == null) {
            return null;
        }

        double speed = definition.speed().resolve(context.scaledValueContext());
        double range = definition.range().resolve(context.scaledValueContext());
        Vec3 direction = resolveDirection(context, launchDirection);
        if (!validLaunch(speed, range, direction)) {
            return null;
        }

        Vec3 position = context.caster().getEyePosition().add(direction.scale(0.45D));
        UUID targetId = context.target() == null ? null : context.target().getUUID();
        VirtualProjectileInstance projectile = new VirtualProjectileInstance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                context.charge(),
                position,
                direction.scale(speed),
                range,
                targetId,
                context.variables()
        );
        PROJECTILES.add(projectile);
        definition.visual().ifPresent(visual -> RuntimeVisualSync.spawn(
                context.level(),
                visualState(
                        projectile,
                        visual,
                        context.level().getGameTime() + Math.max(1L, (long) Math.ceil(range / speed) + 20L),
                        range,
                        definition.hitRadius()
                )
        ));
        return projectile.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        return runtimeId != null && PROJECTILES.removeIf(value -> value.runtimeId().equals(runtimeId));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (VirtualProjectileInstance projectile : List.copyOf(PROJECTILES)) {
            if (!PROJECTILES.contains(projectile)) {
                continue;
            }
            ServerLevel level = event.getServer().getLevel(projectile.dimension());
            if (level == null || !tick(level, projectile)) {
                PROJECTILES.remove(projectile);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PROJECTILES.clear();
    }

    private static boolean tick(ServerLevel level, VirtualProjectileInstance projectile) {
        Entity ownerEntity = level.getEntity(projectile.ownerId());
        if (!(ownerEntity instanceof ServerPlayer owner) || owner.isRemoved()) {
            return false;
        }

        RuntimeDefinition definition = resolveDefinition(level, projectile);
        if (definition == null) {
            return false;
        }

        projectile.incrementTicksLived();
        for (ProjectileBehavior behavior : definition.behaviors()) {
            LivingEntity target = resolveTarget(level, projectile.targetId());
            SkillExecutionContext executionContext = executionContext(
                    level,
                    owner,
                    projectile,
                    target,
                    projectile.position()
            );
            ProjectileBehaviorContext behaviorContext = new ProjectileBehaviorContext(
                    level,
                    owner,
                    target,
                    projectile,
                    executionContext
            );
            if (!behavior.beforeMove(behaviorContext)) {
                expire(level, owner, projectile, definition);
                removeVisual(level, projectile, definition);
                return false;
            }
        }

        Vec3 start = projectile.position();
        Vec3 end = start.add(projectile.velocity());
        BlockHitResult blockHit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                owner
        ));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY
                : start.distanceToSqr(blockHit.getLocation());

        EntityHitCandidate entityHit = findEntityHit(
                level,
                owner,
                projectile,
                start,
                end,
                definition.hitRadius(),
                definition.filter()
        );
        double entityDistance = entityHit == null
                ? Double.POSITIVE_INFINITY
                : start.distanceToSqr(entityHit.position());

        if (entityHit != null && entityDistance <= blockDistance) {
            applyFeatures(
                    level,
                    owner,
                    projectile,
                    definition.entityHitFeatures(),
                    entityHit.target(),
                    entityHit.position()
            );
            ProjectileBehaviorContext hitContext = new ProjectileBehaviorContext(
                    level,
                    owner,
                    entityHit.target(),
                    projectile,
                    executionContext(level, owner, projectile, entityHit.target(), entityHit.position())
            );
            for (ProjectileBehavior behavior : definition.behaviors()) {
                behavior.afterEntityHit(hitContext);
            }
            projectile.hitEntities().add(entityHit.target().getUUID());
            projectile.addPierce();
            if (projectile.pierces() > definition.pierces()) {
                removeVisual(level, projectile, definition);
                return false;
            }
        } else if (blockDistance < Double.POSITIVE_INFINITY) {
            applyFeatures(
                    level,
                    owner,
                    projectile,
                    definition.blockHitFeatures(),
                    null,
                    blockHit.getLocation()
            );
            ProjectileBehaviorContext hitContext = new ProjectileBehaviorContext(
                    level,
                    owner,
                    null,
                    projectile,
                    executionContext(level, owner, projectile, null, blockHit.getLocation())
            );
            for (ProjectileBehavior behavior : definition.behaviors()) {
                behavior.afterBlockHit(hitContext);
            }
            removeVisual(level, projectile, definition);
            return false;
        }

        projectile.setPosition(end);
        projectile.addTravelled(projectile.velocity().length());
        projectile.setVelocity(projectile.velocity().add(0.0D, -definition.gravity(), 0.0D));
        spawnFlightParticle(level, definition.flightParticle(), projectile.position());
        if (projectile.ticksLived() % 4 == 0 && definition.visual().isPresent()) {
            double remainingRange = Math.max(0.0D, projectile.maximumRange() - projectile.travelled());
            double speed = Math.max(1.0E-6D, projectile.velocity().length());
            long expiresAt = level.getGameTime() + Math.max(1L, (long) Math.ceil(remainingRange / speed) + 8L);
            RuntimeVisualSync.update(
                    level,
                    visualState(
                            projectile,
                            definition.visual().get(),
                            expiresAt,
                            projectile.maximumRange(),
                            definition.hitRadius()
                    )
            );
        }

        if (projectile.travelled() >= projectile.maximumRange()) {
            expire(level, owner, projectile, definition);
            removeVisual(level, projectile, definition);
            return false;
        }
        return true;
    }

    private static RuntimeDefinition resolveDefinition(
            ServerLevel level,
            VirtualProjectileInstance projectile
    ) {
        VirtualProjectileDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.VIRTUAL_PROJECTILE_REGISTRY,
                projectile.definitionId(),
                level.registryAccess()
        );
        if (definition == null) {
            return null;
        }
        return new RuntimeDefinition(
                definition.gravity(),
                definition.hitRadius(),
                definition.pierces(),
                definition.filter(),
                definition.flightParticle(),
                definition.behaviors(),
                definition.onEntityHit(),
                definition.onBlockHit(),
                definition.onExpire(),
                definition.visual()
        );
    }

    private static LivingEntity resolveTarget(ServerLevel level, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        Entity entity = level.getEntity(targetId);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static EntityHitCandidate findEntityHit(
            ServerLevel level,
            ServerPlayer owner,
            VirtualProjectileInstance projectile,
            Vec3 start,
            Vec3 end,
            double radius,
            TargetFilterDefinition filter
    ) {
        AABB search = new AABB(start, end).inflate(Math.max(0.0D, radius));
        EntityHitCandidate closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                entity -> !projectile.hitEntities().contains(entity.getUUID())
                        && filter.matches(owner, entity)
        )) {
            AABB box = target.getBoundingBox().inflate(Math.max(0.0D, radius));
            Optional<Vec3> hit = box.clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance < closestDistance) {
                closest = new EntityHitCandidate(target, hit.get());
                closestDistance = distance;
            }
        }
        return closest;
    }

    private static void expire(
            ServerLevel level,
            ServerPlayer owner,
            VirtualProjectileInstance projectile,
            RuntimeDefinition definition
    ) {
        applyFeatures(
                level,
                owner,
                projectile,
                definition.expiryFeatures(),
                null,
                projectile.position()
        );
        ProjectileBehaviorContext context = new ProjectileBehaviorContext(
                level,
                owner,
                resolveTarget(level, projectile.targetId()),
                projectile,
                executionContext(level, owner, projectile, null, projectile.position())
        );
        for (ProjectileBehavior behavior : definition.behaviors()) {
            behavior.onExpire(context);
        }
    }

    private static void applyFeatures(
            ServerLevel level,
            ServerPlayer owner,
            VirtualProjectileInstance projectile,
            List<SkillExecutionFeature> features,
            LivingEntity target,
            Vec3 position
    ) {
        SkillExecutionContext context = executionContext(level, owner, projectile, target, position);
        for (SkillExecutionFeature feature : features) {
            feature.apply(context);
        }
    }

    private static SkillExecutionContext executionContext(
            ServerLevel level,
            ServerPlayer owner,
            VirtualProjectileInstance projectile,
            LivingEntity target,
            Vec3 position
    ) {
        return new SkillExecutionContext(
                level,
                owner,
                projectile.skillId(),
                target,
                position,
                projectile.charge(),
                projectile.variables()
        );
    }

    private static Vec3 resolveDirection(
            SkillExecutionContext context,
            ProjectileLaunchDirection direction
    ) {
        if (direction == ProjectileLaunchDirection.TARGET && context.target() != null) {
            Vec3 delta = context.target().getBoundingBox().getCenter().subtract(context.caster().getEyePosition());
            if (delta.lengthSqr() > 1.0E-8D) {
                return delta.normalize();
            }
        }
        return context.caster().getLookAngle().normalize();
    }

    private static boolean validLaunch(double speed, double range, Vec3 direction) {
        return Double.isFinite(speed)
                && Double.isFinite(range)
                && speed > 0.0D
                && range > 0.0D
                && direction != null
                && direction.lengthSqr() > 1.0E-8D;
    }

    private static void spawnFlightParticle(
            ServerLevel level,
            Optional<Identifier> particle,
            Vec3 position
    ) {
        if (particle.isEmpty()) {
            return;
        }
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particle.get());
        if (type instanceof SimpleParticleType simple) {
            level.sendParticles(
                    simple,
                    position.x,
                    position.y,
                    position.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static RuntimeVisualState visualState(
            VirtualProjectileInstance projectile,
            Identifier visual,
            long expiresAt,
            double range,
            double hitRadius
    ) {
        return new RuntimeVisualState(
                projectile.runtimeId(),
                visual,
                projectile.ownerId(),
                projectile.position(),
                projectile.velocity(),
                List.of(),
                List.of(),
                expiresAt,
                0,
                0,
                projectile.maximumRange() <= 0.0D
                        ? 0.0F
                        : (float) Math.clamp(
                                projectile.travelled() / projectile.maximumRange(),
                                0.0D,
                                1.0D
                        ),
                projectile.runtimeId().getMostSignificantBits(),
                range,
                hitRadius
        );
    }

    private static void removeVisual(
            ServerLevel level,
            VirtualProjectileInstance projectile,
            RuntimeDefinition definition
    ) {
        definition.visual().ifPresent(visual -> RuntimeVisualSync.remove(
                level,
                visualState(projectile, visual, 0L, projectile.maximumRange(), definition.hitRadius())
        ));
    }

    private record EntityHitCandidate(LivingEntity target, Vec3 position) {
    }

    private record RuntimeDefinition(
            double gravity,
            double hitRadius,
            int pierces,
            TargetFilterDefinition filter,
            Optional<Identifier> flightParticle,
            List<ProjectileBehavior> behaviors,
            List<SkillExecutionFeature> entityHitFeatures,
            List<SkillExecutionFeature> blockHitFeatures,
            List<SkillExecutionFeature> expiryFeatures,
            Optional<Identifier> visual
    ) {
    }
}
