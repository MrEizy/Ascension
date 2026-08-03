package net.zic.ascension.api.ascension.core.skill.castable.held;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastSoundDefinition;

import java.util.List;
import java.util.Optional;

public record HeldCastChargeStage(
        double threshold,
        Optional<ParticleFieldDefinition> particleField,
        List<CastSoundDefinition> sounds
) {
    public static final Codec<HeldCastChargeStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("threshold", 0.0D).forGetter(HeldCastChargeStage::threshold),
            ParticleFieldDefinition.CODEC.optionalFieldOf("particle_field").forGetter(HeldCastChargeStage::particleField),
            CastSoundDefinition.CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(HeldCastChargeStage::sounds)
    ).apply(instance, HeldCastChargeStage::new));

    public HeldCastChargeStage {
        threshold = Double.isFinite(threshold) ? Math.clamp(threshold, 0.0D, 1.0D) : 0.0D;
        particleField = particleField == null ? Optional.empty() : particleField;
        sounds = sounds == null ? List.of() : List.copyOf(sounds);
    }
}
