package net.zic.ascension.api.ascension.core.cultivation;

import java.util.List;

public record AuthorityCurve(List<Double> multipliers, double mortalAuthority, double terminalGrowth) {
    public AuthorityCurve {
        multipliers = List.copyOf(multipliers);
        if (multipliers.isEmpty() || !Double.isFinite(mortalAuthority) || mortalAuthority <= 0.0D
                || !Double.isFinite(terminalGrowth) || terminalGrowth <= 1.0D) {
            throw new IllegalArgumentException("Authority requires positive mortal authority, major anchors and terminal growth > 1");
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
}
