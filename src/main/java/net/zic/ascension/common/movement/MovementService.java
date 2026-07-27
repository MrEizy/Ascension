package net.zic.ascension.common.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.movement.MovementAnchor;
import net.zic.ascension.api.core.movement.MovementCollisionPolicy;
import net.zic.ascension.api.core.movement.MovementFailureReason;
import net.zic.ascension.api.core.movement.MovementResult;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.movement.MovementAnchorContainer;

public final class MovementService {
    private static final double STEP_SIZE = 0.25D;

    private MovementService() {
    }

    public static MovementResult move(
            ServerLevel level,
            LivingEntity entity,
            Vec3 desiredDestination,
            MovementCollisionPolicy collisionPolicy,
            boolean preserveVelocity
    ) {
        if (level == null || entity == null || entity.isRemoved() || desiredDestination == null) {
            return MovementResult.failure(
                    entity == null ? Vec3.ZERO : entity.position(),
                    desiredDestination,
                    MovementFailureReason.INVALID_ENTITY
            );
        }

        Vec3 origin = entity.position();
        if (origin.distanceToSqr(desiredDestination) <= 1.0E-8D) {
            return MovementResult.failure(origin, desiredDestination, MovementFailureReason.NO_MOVEMENT);
        }
        if (!level.hasChunkAt(BlockPos.containing(desiredDestination))) {
            return MovementResult.failure(origin, desiredDestination, MovementFailureReason.UNLOADED_DESTINATION);
        }

        Vec3 destination = resolveDestination(level, entity, desiredDestination, collisionPolicy);
        if (destination == null) {
            return MovementResult.failure(origin, desiredDestination, MovementFailureReason.BLOCKED_DESTINATION);
        }

        Vec3 velocity = entity.getDeltaMovement();
        entity.teleportTo(destination.x, destination.y, destination.z);
        entity.setDeltaMovement(preserveVelocity ? velocity : Vec3.ZERO);
        entity.hurtMarked = true;
        return MovementResult.success(origin, destination);
    }

    public static void setAnchor(
            LivingEntity entity,
            Identifier id,
            long duration
    ) {
        if (entity == null || id == null || entity.level().isClientSide()) {
            return;
        }
        long expiresAt = duration <= 0L ? 0L : entity.level().getGameTime() + duration;
        MovementAnchor anchor = new MovementAnchor(
                entity.level().dimension().identifier(),
                entity.position(),
                entity.getYRot(),
                entity.getXRot(),
                expiresAt
        );
        entity.getData(AscensionAttachments.MOVEMENT_ANCHORS).put(id, anchor);
    }

    public static MovementAnchor getAnchor(LivingEntity entity, Identifier id) {
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
            MovementCollisionPolicy policy
    ) {
        if (canOccupy(level, entity, desired)) {
            return desired;
        }
        if (policy == MovementCollisionPolicy.FAIL) {
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
}
