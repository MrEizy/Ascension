package net.zic.ascension.mob_cultivation.generation;

import java.util.Locale;

public enum MobCultivationEliteTier {
    NORMAL(1.0D, 1.0D, 1.0D, 1, 0, 0),
    ELITE(1.25D, 1.20D, 1.50D, 2, 0, 1),
    ANCIENT(1.65D, 1.45D, 2.25D, 4, 1, 1);

    private final double statMultiplier;
    private final double growthMultiplier;
    private final double lootMultiplier;
    private final int particleMultiplier;
    private final int majorRealmBonus;
    private final int minorRealmBonus;

    MobCultivationEliteTier(
            double statMultiplier,
            double growthMultiplier,
            double lootMultiplier,
            int particleMultiplier,
            int majorRealmBonus,
            int minorRealmBonus
    ) {
        this.statMultiplier = statMultiplier;
        this.growthMultiplier = growthMultiplier;
        this.lootMultiplier = lootMultiplier;
        this.particleMultiplier = particleMultiplier;
        this.majorRealmBonus = majorRealmBonus;
        this.minorRealmBonus = minorRealmBonus;
    }

    public double statMultiplier() {
        return statMultiplier;
    }

    public double growthMultiplier() {
        return growthMultiplier;
    }

    public double lootMultiplier() {
        return lootMultiplier;
    }

    public int particleMultiplier() {
        return particleMultiplier;
    }

    public int majorRealmBonus() {
        return majorRealmBonus;
    }

    public int minorRealmBonus() {
        return minorRealmBonus;
    }

    public boolean isElite() {
        return this != NORMAL;
    }

    public static MobCultivationEliteTier parse(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return NORMAL;
        }
    }
}
