package net.thejadeproject.ascension.mob_cultivation;

public record MobCultivationStatProfile(
        double vitality,
        double strength,
        double agility
) {
    public MobCultivationStatProfile add(MobCultivationStatProfile other) {
        return new MobCultivationStatProfile(
                Math.max(0, this.vitality + other.vitality),
                Math.max(0, this.strength + other.strength),
                Math.max(0, this.agility + other.agility)
        );
    }

    public MobCultivationStatProfile multiply(double value) {
        return new MobCultivationStatProfile(
                Math.max(0, this.vitality * value),
                Math.max(0, this.strength * value),
                Math.max(0, this.agility * value)
        );
    }

}