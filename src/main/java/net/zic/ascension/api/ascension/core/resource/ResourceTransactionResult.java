package net.zic.ascension.api.ascension.core.resource;

public record ResourceTransactionResult(
        ResourceTransactionContext context,
        ResourceTransactionStatus status,
        double requestedAmount,
        double resolvedAmount,
        double appliedAmount,
        double amountBefore,
        double amountAfter
) {
    public boolean succeeded() {
        return status == ResourceTransactionStatus.SUCCESS || status == ResourceTransactionStatus.PARTIAL;
    }

    public boolean wasPrevented() {
        return status == ResourceTransactionStatus.CANCELLED || status == ResourceTransactionStatus.IMMUNE;
    }
}
