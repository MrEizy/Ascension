package net.zic.ascension.api.ascension.core.resource;

public record ResourceApplicationResult(
        ResourceTransactionStatus status,
        double amountBefore,
        double amountAfter,
        double appliedAmount
) {
    public static ResourceApplicationResult rejected(double current) {
        return new ResourceApplicationResult(ResourceTransactionStatus.REJECTED, current, current, 0.0D);
    }

    public static ResourceApplicationResult unsupported(double current) {
        return new ResourceApplicationResult(ResourceTransactionStatus.UNSUPPORTED, current, current, 0.0D);
    }
}
