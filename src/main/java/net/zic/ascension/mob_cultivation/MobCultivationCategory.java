package net.zic.ascension.mob_cultivation;

public enum MobCultivationCategory {
    PASSIVE(1.0D, 1.0D, 0.0D),
    HOSTILE(1.5D, 1.15D, 0.15D),
    BOSS(3.0D, 1.35D, 0.40D);

    private final double statMultiplier;
    private final double growthMultiplier;
    private final double lootChanceBonus;

    MobCultivationCategory(
            double statMultiplier,
            double growthMultiplier,
            double lootChanceBonus
    ) {
        this.statMultiplier = statMultiplier;
        this.growthMultiplier = growthMultiplier;
        this.lootChanceBonus = lootChanceBonus;
    }

    public double statMultiplier() {
        return statMultiplier;
    }

    public double growthMultiplier() {
        return growthMultiplier;
    }

    public double lootChanceBonus() {
        return lootChanceBonus;
    }
}
