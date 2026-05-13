package net.thejadeproject.ascension.mob_cultivation;

public record MobCultivationDefinition(
        String realmId,
        int stage,
        MobCultivationStatProfile baseStats
) {
}