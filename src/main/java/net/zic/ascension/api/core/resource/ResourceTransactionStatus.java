package net.zic.ascension.api.core.resource;

public enum ResourceTransactionStatus {
    SUCCESS,
    PARTIAL,
    CANCELLED,
    IMMUNE,
    INVALID,
    UNSUPPORTED,
    REJECTED,
    RECURSION_BLOCKED
}
