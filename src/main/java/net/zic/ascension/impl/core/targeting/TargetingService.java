package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class TargetingService {
    private TargetingService() {
    }

    public static List<TargetingDefinition.Target> radial(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            TargetingDefinition.Filter filter,
            TargetingDefinition.Sort sort,
            int maximumTargets
    ) {
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            return List.of();
        }
        double radiusSqr = radius * radius;
        AABB bounds = new AABB(center, center).inflate(radius);
        return select(
                caster,
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        bounds,
                        target -> target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                                .distanceToSqr(center) <= radiusSqr
                ),
                filter,
                sort,
                maximumTargets,
                target -> true
        );
    }

    public static List<TargetingDefinition.Target> cone(
            ServerLevel level,
            LivingEntity caster,
            double range,
            double angleDegrees,
            TargetingDefinition.Filter filter,
            TargetingDefinition.Sort sort,
            int maximumTargets
    ) {
        if (!Double.isFinite(range) || range <= 0.0D || !Double.isFinite(angleDegrees) || angleDegrees <= 0.0D) {
            return List.of();
        }
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        double cosine = Math.cos(Math.toRadians(Math.clamp(angleDegrees, 0.0D, 360.0D) * 0.5D));
        AABB bounds = caster.getBoundingBox().inflate(range);
        return select(
                caster,
                level.getEntitiesOfClass(LivingEntity.class, bounds),
                filter,
                sort,
                maximumTargets,
                target -> {
                    Vec3 direction = target.position()
                            .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                            .subtract(start);
                    double lengthSqr = direction.lengthSqr();
                    if (lengthSqr > range * range || lengthSqr <= 1.0E-8D) {
                        return false;
                    }
                    return direction.normalize().dot(look) >= cosine;
                }
        );
    }

    public static TargetingDefinition.Target ray(
            ServerLevel level,
            LivingEntity caster,
            double range,
            double width,
            TargetingDefinition.Filter filter
    ) {
        if (!Double.isFinite(range) || range <= 0.0D) {
            return null;
        }
        double resolvedWidth = Double.isFinite(width) ? Math.max(0.0D, width) : 0.0D;
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().normalize().scale(range));
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
        AABB search = new AABB(start, end).inflate(resolvedWidth);
        TargetingDefinition.Target closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (!matches(caster, target, filter)) {
                continue;
            }
            AABB box = target.getBoundingBox().inflate(resolvedWidth);
            Optional<Vec3> hit = box.clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance <= blockDistance && distance < closestDistance) {
                closest = new TargetingDefinition.Target(target, hit.get());
                closestDistance = distance;
            }
        }
        return closest;
    }

    public static TargetingDefinition.Target lookPosition(
            ServerLevel level,
            LivingEntity caster,
            double range,
            boolean includeFluids,
            boolean fallbackToMaximumRange
    ) {
        if (!Double.isFinite(range) || range <= 0.0D) {
            return null;
        }
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().normalize().scale(range));
        BlockHitResult hit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                caster
        ));
        if (hit.getType() == HitResult.Type.MISS) {
            return fallbackToMaximumRange ? TargetingDefinition.Target.position(end) : null;
        }
        return TargetingDefinition.Target.position(hit.getLocation());
    }

    public static List<TargetingDefinition.Target> select(
            LivingEntity caster,
            List<LivingEntity> candidates,
            TargetingDefinition.Filter filter,
            TargetingDefinition.Sort sort,
            int maximumTargets,
            Predicate<LivingEntity> extraFilter
    ) {
        List<LivingEntity> filtered = new ArrayList<>();
        for (LivingEntity target : candidates) {
            if (matches(caster, target, filter) && extraFilter.test(target)) {
                filtered.add(target);
            }
        }
        filtered.sort(comparator(caster, sort));
        int limit = maximumTargets <= 0
                ? filtered.size()
                : Math.min(maximumTargets, filtered.size());
        List<TargetingDefinition.Target> targets = new ArrayList<>(limit);
        for (int index = 0; index < limit; index++) {
            targets.add(TargetingDefinition.Target.entity(filtered.get(index)));
        }
        return List.copyOf(targets);
    }

    public static boolean matches(
            LivingEntity caster,
            LivingEntity target,
            TargetingDefinition.Filter filter
    ) {
        return filter.matches(caster, target)
                && (!filter.requireLineOfSight() || caster.hasLineOfSight(target));
    }

    private static Comparator<LivingEntity> comparator(LivingEntity caster, TargetingDefinition.Sort sort) {
        return switch (sort) {
            case FURTHEST -> Comparator.<LivingEntity>comparingDouble(caster::distanceToSqr).reversed();
            case LOWEST_HEALTH -> Comparator.<LivingEntity>comparingDouble(LivingEntity::getHealth)
                    .thenComparingDouble(caster::distanceToSqr);
            case HIGHEST_HEALTH -> Comparator.<LivingEntity>comparingDouble(LivingEntity::getHealth)
                    .reversed()
                    .thenComparingDouble(caster::distanceToSqr);
            case CLOSEST_TO_VIEW -> Comparator.<LivingEntity>comparingDouble(target -> viewAlignment(caster, target))
                    .reversed()
                    .thenComparingDouble(caster::distanceToSqr);
            case NEAREST -> Comparator.<LivingEntity>comparingDouble(caster::distanceToSqr);
        };
    }

    private static double viewAlignment(LivingEntity caster, LivingEntity target) {
        Vec3 direction = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(caster.getEyePosition());
        if (direction.lengthSqr() <= 1.0E-8D) {
            return 1.0D;
        }
        return direction.normalize().dot(caster.getLookAngle().normalize());
    }
}
