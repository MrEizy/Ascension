package net.zic.ascension.impl.core.skill.castable.held;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.impl.core.skill.castable.held.execution.ProjectileReleaseExecution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class HeldCastProjectileManager {
    private static final List<VirtualProjectile> PROJECTILES = new ArrayList<>();

    private HeldCastProjectileManager() {
    }

    public static void spawn(HeldCastExecutionContext context, ProjectileReleaseExecution execution) {
        double speed = execution.speed().resolve(context.scaledValueContext(null));
        double range = execution.range().resolve(context.scaledValueContext(null));
        if (!Double.isFinite(speed) || !Double.isFinite(range) || speed <= 0.0D || range <= 0.0D) {
            return;
        }

        Vec3 direction = context.caster().getLookAngle().normalize();
        Vec3 position = context.caster().getEyePosition().add(direction.scale(0.45D));
        PROJECTILES.add(new VirtualProjectile(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                context.chargeTicks(),
                context.maximumChargeTicks(),
                context.charge(),
                position,
                direction.scale(speed),
                range
        ));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<VirtualProjectile> iterator = PROJECTILES.iterator();
        while (iterator.hasNext()) {
            VirtualProjectile projectile = iterator.next();
            ServerLevel level = event.getServer().getLevel(projectile.dimension);
            if (level == null || !tick(level, projectile)) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PROJECTILES.clear();
    }

    private static boolean tick(ServerLevel level, VirtualProjectile projectile) {
        Entity casterEntity = level.getEntity(projectile.caster);
        if (!(casterEntity instanceof ServerPlayer caster) || caster.isRemoved()) {
            return false;
        }

        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                projectile.skill,
                level.registryAccess()
        );
        if (!(skill instanceof HeldCastSkill heldSkill)
                || !(heldSkill.execution() instanceof ProjectileReleaseExecution execution)) {
            return false;
        }

        Vec3 start = projectile.position;
        Vec3 end = start.add(projectile.velocity);
        BlockHitResult blockHit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));
        double blockDistance = blockHit.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY
                : start.distanceToSqr(blockHit.getLocation());

        EntityHitCandidate entityHit = findEntityHit(
                level,
                caster,
                projectile,
                start,
                end,
                execution.hitRadius()
        );
        double entityDistance = entityHit == null
                ? Double.POSITIVE_INFINITY
                : start.distanceToSqr(entityHit.position());

        if (entityHit != null && entityDistance <= blockDistance) {
            applyFeatures(
                    level,
                    caster,
                    projectile,
                    execution,
                    entityHit.target(),
                    entityHit.position()
            );
            projectile.hitEntities.add(entityHit.target().getUUID());
            projectile.pierces++;
            if (projectile.pierces > execution.pierces()) {
                return false;
            }
        } else if (blockDistance < Double.POSITIVE_INFINITY) {
            applyFeatures(level, caster, projectile, execution, null, blockHit.getLocation());
            return false;
        }

        projectile.position = end;
        projectile.travelled += projectile.velocity.length();
        projectile.velocity = projectile.velocity.add(0.0D, -execution.gravity(), 0.0D);
        spawnFlightParticle(level, execution, projectile.position);
        return projectile.travelled < projectile.maximumRange;
    }

    private static EntityHitCandidate findEntityHit(
            ServerLevel level,
            ServerPlayer caster,
            VirtualProjectile projectile,
            Vec3 start,
            Vec3 end,
            double radius
    ) {
        AABB search = new AABB(start, end).inflate(Math.max(0.0D, radius));
        EntityHitCandidate closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                entity -> entity != caster
                        && !entity.isRemoved()
                        && !caster.isAlliedTo(entity)
                        && !projectile.hitEntities.contains(entity.getUUID())
        )) {
            AABB box = target.getBoundingBox().inflate(Math.max(0.0D, radius));
            var hit = box.clip(start, end);
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

    private static void applyFeatures(
            ServerLevel level,
            ServerPlayer caster,
            VirtualProjectile projectile,
            ProjectileReleaseExecution execution,
            LivingEntity target,
            Vec3 position
    ) {
        HeldCastExecutionContext executionContext = new HeldCastExecutionContext(
                level,
                caster,
                projectile.skill,
                new net.zic.ascension.api.core.skill.castable.held.HeldCastData(),
                projectile.chargeTicks,
                projectile.maximumChargeTicks,
                projectile.charge
        );
        HeldCastReleaseContext release = new HeldCastReleaseContext(executionContext, target, position);
        for (HeldCastReleaseFeature feature : execution.features()) {
            feature.apply(release);
        }
    }

    private static void spawnFlightParticle(
            ServerLevel level,
            ProjectileReleaseExecution execution,
            Vec3 position
    ) {
        if (execution.flightParticle().isEmpty()) {
            return;
        }
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(execution.flightParticle().get());
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

    private record EntityHitCandidate(LivingEntity target, Vec3 position) {
    }

    private static final class VirtualProjectile {
        private final ResourceKey<Level> dimension;
        private final UUID caster;
        private final Identifier skill;
        private final int chargeTicks;
        private final int maximumChargeTicks;
        private final double charge;
        private final double maximumRange;
        private final Set<UUID> hitEntities = new HashSet<>();
        private Vec3 position;
        private Vec3 velocity;
        private double travelled;
        private int pierces;

        private VirtualProjectile(
                ResourceKey<Level> dimension,
                UUID caster,
                Identifier skill,
                int chargeTicks,
                int maximumChargeTicks,
                double charge,
                Vec3 position,
                Vec3 velocity,
                double maximumRange
        ) {
            this.dimension = dimension;
            this.caster = caster;
            this.skill = skill;
            this.chargeTicks = chargeTicks;
            this.maximumChargeTicks = maximumChargeTicks;
            this.charge = charge;
            this.position = position;
            this.velocity = velocity;
            this.maximumRange = maximumRange;
        }
    }
}
