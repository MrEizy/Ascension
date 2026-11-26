package net.zic.ascension.api.ascension.core.cultivation;

import java.util.EnumSet;
import java.util.Set;

public final class AuthorityCombatMath {
    public enum Channel { BODY, ESSENCE, SOUL }

    private AuthorityCombatMath() {}

    public static EnumSet<Channel> channels(Set<String> classifications, String path, String damageType, boolean skill) {
        EnumSet<Channel> result = EnumSet.noneOf(Channel.class);
        if (classifications.contains("ascension:body")) result.add(Channel.BODY);
        if (classifications.contains("ascension:essence")) result.add(Channel.ESSENCE);
        if (classifications.contains("ascension:soul")) result.add(Channel.SOUL);
        if (!result.isEmpty()) return result;
        if (classifications.contains("ascension:qi")) return EnumSet.of(Channel.ESSENCE);
        if ("ascension:foundation/body".equals(path)) return EnumSet.of(Channel.BODY);
        if ("ascension:foundation/essence".equals(path)) return EnumSet.of(Channel.ESSENCE);
        if ("ascension:foundation/soul".equals(path)) return EnumSet.of(Channel.SOUL);
        if (damageType != null && damageType.startsWith("ascension:") && damageType.endsWith("_qi")) {
            return EnumSet.of(Channel.ESSENCE);
        }
        if (skill && path != null && path.startsWith("ascension:elemental/")) return EnumSet.of(Channel.ESSENCE);
        if ("minecraft:magic".equals(damageType) || "minecraft:indirect_magic".equals(damageType)
                || "minecraft:dragon_breath".equals(damageType)) return EnumSet.of(Channel.ESSENCE);
        return EnumSet.of(Channel.BODY);
    }

    public static double ratio(Set<Channel> channels, double[] attacker, double[] defender) {
        if (channels.isEmpty() || attacker.length != 3 || defender.length != 3) {
            throw new IllegalArgumentException("Authority combat needs channels and three foundation values");
        }
        double logRatio = 0.0D;
        for (Channel channel : channels) {
            double a = attacker[channel.ordinal()];
            double d = defender[channel.ordinal()];
            if (!Double.isFinite(a) || !Double.isFinite(d) || a <= 0.0D || d <= 0.0D) {
                throw new IllegalArgumentException("Combat authority must be positive and finite");
            }
            logRatio += Math.log(a) - Math.log(d);
        }
        double ratio = Math.exp(logRatio / channels.size());
        if (!Double.isFinite(ratio) || ratio <= 0.0D) {
            throw new IllegalArgumentException("Combat authority ratio is outside double precision range");
        }
        return ratio;
    }

    public static int factorCount(double ratio) {
        if (!Double.isFinite(ratio) || ratio <= 0.0D) throw new IllegalArgumentException("Invalid authority ratio");
        return Math.max(1, (int) Math.ceil(-Math.log10(ratio) / 4.0D));
    }
}
