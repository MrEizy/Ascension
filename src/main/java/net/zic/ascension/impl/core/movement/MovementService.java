package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public final class MovementService {
    private static final double STEP_SIZE = 0.25D;

    private MovementService() {
    }

    public static Result move(
            ServerLevel level,
            LivingEntity entity,
            Vec3 desiredDestination,
            CollisionPolicy collisionPolicy,
            boolean preserveVelocity
    ) {
        if (level == null || entity == null || entity.isRemoved() || desiredDestination == null) {
            return Result.failure(
                    entity == null ? Vec3.ZERO : entity.position(),
                    desiredDestination,
                    FailureReason.INVALID_ENTITY
            );
        }
        Vec3 origin = entity.position();
        if (origin.distanceToSqr(desiredDestination) <= 1.0E-8D) {
            return Result.failure(origin, desiredDestination, FailureReason.NO_MOVEMENT);
        }
        if (!level.hasChunkAt(BlockPos.containing(desiredDestination))) {
            return Result.failure(origin, desiredDestination, FailureReason.UNLOADED_DESTINATION);
        }
        Vec3 destination = resolveDestination(level, entity, desiredDestination, collisionPolicy);
        if (destination == null) {
            return Result.failure(origin, desiredDestination, FailureReason.BLOCKED_DESTINATION);
        }
        Vec3 velocity = entity.getDeltaMovement();
        entity.teleportTo(destination.x, destination.y, destination.z);
        entity.setDeltaMovement(preserveVelocity ? velocity : Vec3.ZERO);
        entity.hurtMarked = true;
        return Result.success(origin, destination);
    }

    public static void setAnchor(LivingEntity entity, Identifier id, long duration) {
        if (entity == null || id == null || entity.level().isClientSide()) {
            return;
        }
        long expiresAt = duration <= 0L ? 0L : entity.level().getGameTime() + duration;
        entity.getData(AscensionAttachments.MOVEMENT_ANCHORS).put(id, new MovementAnchorContainer.Anchor(
                entity.level().dimension().identifier(),
                entity.position(),
                entity.getYRot(),
                entity.getXRot(),
                expiresAt
        ));
    }

    public static MovementAnchorContainer.Anchor getAnchor(LivingEntity entity, Identifier id) {
        if (entity == null || id == null) {
            return null;
        }
        return entity.getData(AscensionAttachments.MOVEMENT_ANCHORS).get(id, entity.level().getGameTime());
    }

    public static boolean removeAnchor(LivingEntity entity, Identifier id) {
        return entity != null
                && id != null
                && entity.getData(AscensionAttachments.MOVEMENT_ANCHORS).remove(id);
    }

    private static Vec3 resolveDestination(
            ServerLevel level,
            LivingEntity entity,
            Vec3 desired,
            CollisionPolicy policy
    ) {
        if (canOccupy(level, entity, desired)) {
            return desired;
        }
        if (policy == CollisionPolicy.FAIL) {
            return null;
        }
        Vec3 origin = entity.position();
        Vec3 delta = desired.subtract(origin);
        double length = delta.length();
        if (length <= 1.0E-8D) {
            return null;
        }
        Vec3 direction = delta.scale(1.0D / length);
        Vec3 lastValid = origin;
        for (double travelled = STEP_SIZE; travelled <= length; travelled += STEP_SIZE) {
            Vec3 candidate = origin.add(direction.scale(Math.min(travelled, length)));
            if (!level.hasChunkAt(BlockPos.containing(candidate)) || !canOccupy(level, entity, candidate)) {
                break;
            }
            lastValid = candidate;
        }
        return lastValid.distanceToSqr(origin) <= 1.0E-8D ? null : lastValid;
    }

    private static boolean canOccupy(ServerLevel level, LivingEntity entity, Vec3 position) {
        Vec3 delta = position.subtract(entity.position());
        AABB moved = entity.getBoundingBox().move(delta);
        return level.noCollision(entity, moved);
    }

    public enum CollisionPolicy implements StringRepresentable {
        FAIL("fail"),
        STOP_BEFORE_COLLISION("stop_before_collision");

        public static final Codec<CollisionPolicy> CODEC = StringRepresentable.fromEnum(CollisionPolicy::values);

        private final String name;

        CollisionPolicy(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum FailureReason {
        INVALID_ENTITY,
        MISSING_TARGET,
        MISSING_ANCHOR,
        WRONG_DIMENSION,
        UNLOADED_DESTINATION,
        BLOCKED_DESTINATION,
        INVALID_DIRECTION,
        NO_MOVEMENT
    }

    public record Result(boolean succeeded, Vec3 origin, Vec3 destination, FailureReason failureReason) {
        public static Result success(Vec3 origin, Vec3 destination) {
            return new Result(true, origin, destination, null);
        }

        public static Result failure(Vec3 origin, Vec3 destination, FailureReason reason) {
            return new Result(false, origin, destination, reason);
        }
    }
}
