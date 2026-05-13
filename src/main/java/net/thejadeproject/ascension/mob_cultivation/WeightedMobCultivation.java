package net.thejadeproject.ascension.mob_cultivation;

public record WeightedMobCultivation(
        MobCultivationDefinition definition,
        int weight
) {
    public WeightedMobCultivation {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
    }
}