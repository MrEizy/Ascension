package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.util.Mth;
import net.zic.ascension.Config;

public final class MedicinalCultivationGain {
    private MedicinalCultivationGain() {
    }

    public static double baseFraction(int tier, double potency, double scale, double absorption) {
        double power = Mth.clamp(potency, 0.0D, 64.0D) * scale * Mth.clamp(absorption, 0.0D, 1.0D);
        return fractionFromPower(tier, power);
    }

    public static double fractionFromPower(int tier, double power) {
        int rankTier = Mth.clamp(tier, 0, 5);
        return Math.min(0.03D + 0.008D * rankTier,
                power * 0.002D * Math.pow(1.35D, rankTier));
    }

    public static double realmEffectiveness(int tier, int currentMajorRealm) {
        if (currentMajorRealm < 0) return 1.0D;
        int maximumRealm = switch (Mth.clamp(tier, 0, 5)) {
            case 1 -> Config.ALCHEMY_SERVER.PROFOUND_PILL_MAX_REALM.get();
            case 2 -> Config.ALCHEMY_SERVER.HEAVEN_PILL_MAX_REALM.get();
            case 3 -> Config.ALCHEMY_SERVER.SAINT_PILL_MAX_REALM.get();
            case 4 -> Config.ALCHEMY_SERVER.GOD_PILL_MAX_REALM.get();
            case 5 -> Config.ALCHEMY_SERVER.HEAVENS_PATH_PILL_MAX_REALM.get();
            default -> Config.ALCHEMY_SERVER.ORDINARY_PILL_MAX_REALM.get();
        };
        int realmsAbove = Math.max(0, currentMajorRealm - maximumRealm);
        if (realmsAbove == 0) return 1.0D;
        return Math.max(Config.ALCHEMY_SERVER.CULTIVATION_PILL_MINIMUM_EFFECTIVENESS.get(),
                Math.pow(Config.ALCHEMY_SERVER.CULTIVATION_PILL_REALM_MULTIPLIER.get(), realmsAbove));
    }
}
