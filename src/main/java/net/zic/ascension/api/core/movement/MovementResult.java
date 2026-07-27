package net.zic.ascension.api.core.movement;

import net.minecraft.world.phys.Vec3;

public record MovementResult(
        boolean succeeded,
        Vec3 origin,
        Vec3 destination,
        MovementFailureReason failureReason
) {
    public static MovementResult success(Vec3 origin, Vec3 destination) {
        return new MovementResult(true, origin, destination, null);
    }

    public static MovementResult failure(Vec3 origin, Vec3 destination, MovementFailureReason reason) {
        return new MovementResult(false, origin, destination, reason);
    }
}
