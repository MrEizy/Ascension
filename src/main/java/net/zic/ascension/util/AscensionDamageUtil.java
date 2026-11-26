package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public class AscensionDamageUtil {
    public static final Identifier DAMAGE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage");
    public static final Identifier RESISTANCE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"resistance");

    private static double positiveStat(double stat) {
        return Double.isFinite(stat) ? Math.max(0, stat) : 0;
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
