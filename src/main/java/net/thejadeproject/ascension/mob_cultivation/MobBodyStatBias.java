package net.thejadeproject.ascension.mob_cultivation;

public record MobBodyStatBias(
        double vitality,
        double strength,
        double agility
) {
    public static final MobBodyStatBias NONE = new MobBodyStatBias(0, 0, 0);

    public MobCultivationStatProfile asProfile() {
        return new MobCultivationStatProfile(vitality, strength, agility);
    }
}