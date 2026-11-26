package net.zic.ascension.api.ascension.core.attribute;

public final class AttributeScalingMath {
    public static final double MAX_CONTRIBUTION = Float.MAX_VALUE / 1024.0D;
    private AttributeScalingMath() {}

    public static double power(double stat, double coefficient, double scale, double exponent) {
        double x = positiveStat(stat);
        if (x == 0 || coefficient == 0) return 0;
        double ratio = x / scale;
        double logTerm = Double.isFinite(ratio) ? Math.log1p(ratio) : Math.log(x) - Math.log(scale);
        return finiteContribution(Math.exp(Math.log(coefficient) + Math.log(x)
                + (exponent - 1.0D) * logTerm));
    }

    public static double saturation(double stat, double coefficient, double scale) {
        double x = positiveStat(stat);
        if (x == 0 || coefficient == 0) return 0;
        return finiteContribution(coefficient / (1.0D + scale / x));
    }

    public static double positiveStat(double stat) {
        return Double.isFinite(stat) ? Math.max(0, stat) : 0;
    }

    public static double finiteContribution(double value) {
        return Double.isNaN(value) ? 0 : Math.clamp(value, 0, MAX_CONTRIBUTION);
    }

    public static double excessArmorMultiplier(double armor, double resistance) {
        double excess = Math.max(0.0D, positiveStat(armor) - 20.0D);
        if (excess <= resistance) return 1.0D / (1.0D + excess / resistance);
        double ratio = resistance / excess;
        return ratio / (1.0D + ratio);
    }

    public static double armorEffectiveness(double damage, double toughness, double vanillaRemaining) {
        if (!Double.isFinite(damage) || damage <= 0 || !Double.isFinite(vanillaRemaining)) return 1;
        double expectedReduction = Math.clamp(20.0D - damage / (2.0D + positiveStat(toughness) / 4.0D), 4.0D, 20.0D) / 25.0D;
        double effectiveness = Math.clamp((1.0D - vanillaRemaining / damage) / expectedReduction, 0.0D, 1.0D);
        return effectiveness > 1.0D - 1.0E-6D ? 1.0D : effectiveness;
    }

    public static double excessArmorMultiplier(double armor, double resistance, double effectiveness) {
        return (1.0D - effectiveness) + effectiveness * excessArmorMultiplier(armor, resistance);
    }
}
