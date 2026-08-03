package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record CastSoundDefinition(
        Identifier sound,
        int interval,
        int initialDelay,
        float volume,
        float pitch,
        float pitchVariance
) {
    public static final Codec<CastSoundDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(CastSoundDefinition::sound),
            Codec.intRange(1, 1200).optionalFieldOf("interval", 40).forGetter(CastSoundDefinition::interval),
            Codec.intRange(0, 1200).optionalFieldOf("initial_delay", 0).forGetter(CastSoundDefinition::initialDelay),
            Codec.FLOAT.optionalFieldOf("volume", 0.35F).forGetter(CastSoundDefinition::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(CastSoundDefinition::pitch),
            Codec.FLOAT.optionalFieldOf("pitch_variance", 0.0F).forGetter(CastSoundDefinition::pitchVariance)
    ).apply(instance, CastSoundDefinition::new));

    public CastSoundDefinition {
        interval = Math.max(1, interval);
        initialDelay = Math.max(0, initialDelay);
        volume = Float.isFinite(volume) ? Math.clamp(volume, 0.0F, 4.0F) : 0.35F;
        pitch = Float.isFinite(pitch) ? Math.clamp(pitch, 0.01F, 4.0F) : 1.0F;
        pitchVariance = Float.isFinite(pitchVariance)
                ? Math.clamp(pitchVariance, 0.0F, 2.0F)
                : 0.0F;
    }

    public boolean shouldPlay(int ticksElapsed) {
        return ticksElapsed >= initialDelay
                && Math.floorMod(ticksElapsed - initialDelay, interval) == 0;
    }
}
