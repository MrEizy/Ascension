package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record RealmEffectivenessDefinition(List<Double> multipliers, double mortalAuthority, double terminalGrowth,
                             double existentialProtection) {
    public static final double DEFAULT_EXISTENTIAL_PROTECTION = 0.25D;

    public RealmEffectivenessDefinition(List<Double> multipliers, double mortalAuthority, double terminalGrowth) {
        this(multipliers, mortalAuthority, terminalGrowth, DEFAULT_EXISTENTIAL_PROTECTION);
    }

    public RealmEffectivenessDefinition {
        multipliers = List.copyOf(multipliers);
        if (multipliers.isEmpty() || !Double.isFinite(mortalAuthority) || mortalAuthority <= 0.0D
                || !Double.isFinite(terminalGrowth) || terminalGrowth <= 1.0D) {
            throw new IllegalArgumentException("Authority requires positive mortal authority, major anchors and terminal growth > 1");
        }
        if (!Double.isFinite(existentialProtection) || existentialProtection < 0.0D || existentialProtection > 1.0D) {
            throw new IllegalArgumentException("Existential protection must be between 0 and 1");
        }
        double previous = mortalAuthority;
        for (double anchor : multipliers) {
            if (!Double.isFinite(anchor) || anchor <= previous) {
                throw new IllegalArgumentException("Authority anchors must be finite and strictly increasing above mortal authority");
            }
            previous = anchor;
        }
        if (!Double.isFinite(previous * terminalGrowth)) {
            throw new IllegalArgumentException("Terminal authority overflows double");
        }
    }

    public double at(int major, int minor, int maxMinor) {
        if (major == -1) return mortalAuthority;
        if (major < 0 || major >= multipliers.size() || minor < 0 || maxMinor < 0) {
            throw new IllegalArgumentException("Unsupported realm: " + major + "/" + minor + " (max minor " + maxMinor + ")");
        }
        double low = multipliers.get(major);
        if (minor == 0) return low;
        double high = major + 1 < multipliers.size()
                ? multipliers.get(major + 1) : low * terminalGrowth;
        double fraction = minor <= maxMinor
                ? minor / (maxMinor + 1.0D)
                : 1.0D - 1.0D / ((maxMinor + 1.0D) * (minor - (double) maxMinor + 1.0D));
        double value = Math.exp(Math.log(low) + fraction * (Math.log(high) - Math.log(low)));
        return Math.max(low, Math.min(Math.nextDown(high), value));
    }

    private record Raw(List<Double> multipliers, double mortalAuthority, double terminalGrowth, double existentialProtection) {
        private static final Codec<Raw> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.listOf().fieldOf("multipliers").forGetter(Raw::multipliers),
                Codec.DOUBLE.optionalFieldOf("mortal_authority", 0.1D).forGetter(Raw::mortalAuthority),
                Codec.DOUBLE.optionalFieldOf("terminal_growth", 2.0D).forGetter(Raw::terminalGrowth),
                Codec.DOUBLE.optionalFieldOf("existential_protection", RealmEffectivenessDefinition.DEFAULT_EXISTENTIAL_PROTECTION)
                        .forGetter(Raw::existentialProtection)
        ).apply(instance, Raw::new));
    }

    public static final Codec<RealmEffectivenessDefinition> CODEC = Raw.CODEC.comapFlatMap(raw -> {
        try {
            return DataResult.success(new RealmEffectivenessDefinition(raw.multipliers(), raw.mortalAuthority(), raw.terminalGrowth(), raw.existentialProtection()));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, curve -> new Raw(curve.multipliers(), curve.mortalAuthority(), curve.terminalGrowth(), curve.existentialProtection()));
}
