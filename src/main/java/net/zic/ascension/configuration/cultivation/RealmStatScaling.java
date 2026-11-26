package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RealmStatScaling(double majorGrowth, double minorGrowth) {
    public static final Codec<RealmStatScaling> CODEC = RecordCodecBuilder.<RealmStatScaling>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("major_growth").forGetter(RealmStatScaling::majorGrowth),
            Codec.DOUBLE.fieldOf("minor_growth").forGetter(RealmStatScaling::minorGrowth)
    ).apply(instance, RealmStatScaling::new)).validate(value ->
            Double.isFinite(value.majorGrowth) && value.majorGrowth > 0 && nonNegativeFinite(value.minorGrowth)
                    ? DataResult.success(value) : DataResult.error(() -> "Realm growth must be finite; major_growth > 0, minor_growth >= 0"));

    public double at(double base, int major, int minor, int lastRegularMinor) {
        if (major < 0) return 0;
        return finite(base * Math.pow(majorGrowth, major) * (1 + minorGrowth * Math.min(Math.max(0, minor), lastRegularMinor)));
    }

    private static boolean nonNegativeFinite(double value) {
        return Double.isFinite(value) && value >= 0;
    }

    private static double finite(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Stat scaling produced a non-finite value");
        return value;
    }
}
