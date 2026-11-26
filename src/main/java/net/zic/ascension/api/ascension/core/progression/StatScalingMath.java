package net.zic.ascension.api.ascension.core.progression;

/**
 * The equations I'm using to scale essentially
 */
public final class StatScalingMath {
    private StatScalingMath() {}

    public static double realm(double base, double majorGrowth, double minorGrowth, int major, int minor, int lastRegularMinor) {
        if (major < 0) return 0;
        return finite(base * Math.pow(majorGrowth, major) * (1 + minorGrowth * Math.min(Math.max(0, minor), lastRegularMinor)));
    }

    public static double bloodlineFlat(double total, double threshold, double excessFlatRate) {
        return finite(Math.min(total, threshold) + Math.max(0, total - threshold) * excessFlatRate);
    }

    public static double bloodlineMultiplier(double total, double threshold, double excessMultiplierRate) {
        return finite(Math.max(0, total - threshold) * excessMultiplierRate);
    }

    public static double purityFraction(double continuous, double milestonesReached, double total, int purity) {
        if (total == 0) return 0;
        return (continuous * Math.clamp(purity, 0, 100) / 100.0 + milestonesReached) / total;
    }

    public static boolean nonNegativeFinite(double value) {
        return Double.isFinite(value) && value >= 0;
    }

    private static double finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Stat scaling produced a non-finite value");
        return value;
    }
}
