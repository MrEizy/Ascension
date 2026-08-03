package net.zic.ascension.impl.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FrozenStateData {
    public static final Codec<FrozenStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("buildup", 0.0D).forGetter(FrozenStateData::buildup),
            Codec.INT.optionalFieldOf("decay_delay", 0).forGetter(FrozenStateData::decayDelay),
            Codec.INT.optionalFieldOf("visual_ticks", 0).forGetter(FrozenStateData::visualTicks)
    ).apply(instance, FrozenStateData::new));

    private double buildup;
    private int decayDelay;
    private int visualTicks;

    public FrozenStateData() {
        this(0.0D, 0, 0);
    }

    private FrozenStateData(double buildup, int decayDelay, int visualTicks) {
        this.buildup = Math.clamp(buildup, 0.0D, 1.0D);
        this.decayDelay = Math.max(0, decayDelay);
        this.visualTicks = Math.max(0, visualTicks);
    }

    public double buildup() {
        return buildup;
    }

    public int decayDelay() {
        return decayDelay;
    }

    public int visualTicks() {
        return visualTicks;
    }

    public void setBuildup(double value) {
        buildup = Math.clamp(value, 0.0D, 1.0D);
    }

    public void setDecayDelay(int value) {
        decayDelay = Math.max(0, value);
    }

    public void setVisualTicks(int value) {
        visualTicks = Math.max(0, value);
    }

    public boolean isActive() {
        return buildup > 0.0D || decayDelay > 0 || visualTicks > 0;
    }
}
