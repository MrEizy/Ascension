package net.zic.ascension.mob_cultivation;

public enum MobCultivationCategory {
    PASSIVE(0.08D, 1.0D, 1.0D, 0.0D),
    HOSTILE(0.20D, 1.5D, 1.15D, 0.15D),
    BOSS(1.0D, 3.0D, 1.35D, 0.40D);

    private final double cultivationChance;
    private final double statMultiplier;
    private final double growthMultiplier;
    private final double lootChanceBonus;

    MobCultivationCategory(
            double cultivationChance,
            double statMultiplier,
            double growthMultiplier,
            double lootChanceBonus
    ) {
        this.cultivationChance = cultivationChance;
        this.statMultiplier = statMultiplier;
        this.growthMultiplier = growthMultiplier;
        this.lootChanceBonus = lootChanceBonus;
    }

    public double cultivationChance() {
        return cultivationChance;
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
