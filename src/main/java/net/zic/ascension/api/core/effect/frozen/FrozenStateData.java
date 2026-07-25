package net.zic.ascension.api.core.effect.frozen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FrozenStateData {
    public static final Codec<FrozenStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("buildup", 0.0D).forGetter(FrozenStateData::buildup),
            Codec.INT.optionalFieldOf("decay_delay", 0).forGetter(FrozenStateData::decayDelay)
    ).apply(instance, FrozenStateData::new));

    private double buildup;
    private int decayDelay;

    public FrozenStateData() {
        this(0.0D, 0);
    }

    private FrozenStateData(double buildup, int decayDelay) {
        this.buildup = Math.clamp(buildup, 0.0D, 1.0D);
        this.decayDelay = Math.max(0, decayDelay);
    }

    public double buildup() { return buildup; }
    public int decayDelay() { return decayDelay; }
    public void setBuildup(double value) { buildup = Math.clamp(value, 0.0D, 1.0D); }
    public void setDecayDelay(int value) { decayDelay = Math.max(0, value); }
}
