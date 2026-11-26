package net.zic.ascension.api.ascension.core.attribute;

public record RecoveryScaling(double minimumSeconds, double maximumSeconds,
                              double healthThreshold, double sleepFraction) {
    public static final RecoveryScaling DEFAULT = new RecoveryScaling(60, 1200, 100000, 0.25);

    public RecoveryScaling {
        if (!Double.isFinite(minimumSeconds) || minimumSeconds < 1
                || !Double.isFinite(maximumSeconds) || maximumSeconds < minimumSeconds
                || !Double.isFinite(healthThreshold) || healthThreshold <= 0
                || !Double.isFinite(sleepFraction) || sleepFraction < 0 || sleepFraction > 1) {
            throw new IllegalArgumentException("Invalid recovery scaling: expected 1 <= minimum <= maximum, positive threshold, and sleep fraction in [0, 1]");
        }
    }

    public double recoverySeconds(double maximumHealth) {
        if (!Double.isFinite(maximumHealth) || maximumHealth <= 0) return minimumSeconds;
        double fraction = 1.0 / (1.0 + healthThreshold / maximumHealth);
        return minimumSeconds + (maximumSeconds - minimumSeconds) * fraction;
    }

    public double healthPerSecond(double maximumHealth) {
        if (!Double.isFinite(maximumHealth) || maximumHealth <= 0) return 0;
        return maximumHealth / recoverySeconds(maximumHealth);
    }

    public double sleepHealing(double maximumHealth, double currentHealth) {
        if (!Double.isFinite(maximumHealth) || !Double.isFinite(currentHealth) || currentHealth <= 0) return 0;
        return Math.max(0, maximumHealth - currentHealth) * sleepFraction;
    }
}
