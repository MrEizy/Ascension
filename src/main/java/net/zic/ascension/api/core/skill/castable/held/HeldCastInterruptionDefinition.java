package net.zic.ascension.api.core.skill.castable.held;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;

public record HeldCastInterruptionDefinition(
        ScaledValue damageThreshold,
        int accumulationWindow,
        boolean releaseAfterMinimum
) {
    public static final MapCodec<HeldCastInterruptionDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("damage_threshold").forGetter(HeldCastInterruptionDefinition::damageThreshold),
            Codec.intRange(1, 1200).optionalFieldOf("accumulation_window", 20)
                    .forGetter(HeldCastInterruptionDefinition::accumulationWindow),
            Codec.BOOL.optionalFieldOf("release_after_minimum", false)
                    .forGetter(HeldCastInterruptionDefinition::releaseAfterMinimum)
    ).apply(instance, HeldCastInterruptionDefinition::new));

    public boolean shouldInterrupt(HeldCastData data, int ticksElapsed, double damage, ScaledValueContext context) {
        double threshold = damageThreshold.resolve(context);
        if (!Double.isFinite(threshold) || threshold <= 0.0D) {
            return false;
        }
        return data.addInterruptionDamage(damage, ticksElapsed, accumulationWindow) >= threshold;
    }
}
