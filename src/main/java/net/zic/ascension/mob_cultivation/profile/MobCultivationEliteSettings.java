package net.zic.ascension.mob_cultivation.profile;

public record MobCultivationEliteSettings(
        boolean enabled,
        double chance,
        double ancientChance,
        int extraTraitRolls
) {
    public static final MobCultivationEliteSettings DEFAULT = new MobCultivationEliteSettings(
            true,
            0.025D,
            0.08D,
            1
    );

    public MobCultivationEliteSettings {
        chance = Math.clamp(chance, 0.0D, 1.0D);
        ancientChance = Math.clamp(ancientChance, 0.0D, 1.0D);
        extraTraitRolls = Math.max(0, extraTraitRolls);
    }
}
