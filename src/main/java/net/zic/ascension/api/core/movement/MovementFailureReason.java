package net.zic.ascension.api.core.movement;

public enum MovementFailureReason {
    INVALID_ENTITY,
    INVALID_DIRECTION,
    MISSING_TARGET,
    MISSING_ANCHOR,
    WRONG_DIMENSION,
    UNLOADED_DESTINATION,
    BLOCKED_DESTINATION,
    NO_MOVEMENT
}
