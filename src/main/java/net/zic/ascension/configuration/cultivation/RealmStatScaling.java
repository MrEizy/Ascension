package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.progression.StatScalingMath;

public record RealmStatScaling(double majorGrowth, double minorGrowth) {
    public static final Codec<RealmStatScaling> CODEC = RecordCodecBuilder.<RealmStatScaling>create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("major_growth").forGetter(RealmStatScaling::majorGrowth),
            Codec.DOUBLE.fieldOf("minor_growth").forGetter(RealmStatScaling::minorGrowth)
    ).apply(instance, RealmStatScaling::new)).validate(value ->
            Double.isFinite(value.majorGrowth) && value.majorGrowth > 0 && StatScalingMath.nonNegativeFinite(value.minorGrowth)
                    ? DataResult.success(value) : DataResult.error(() -> "Realm growth must be finite; major_growth > 0, minor_growth >= 0"));

    public double at(double base, int major, int minor, int lastRegularMinor) {
        return StatScalingMath.realm(base, majorGrowth, minorGrowth, major, minor, lastRegularMinor);
    }
}
