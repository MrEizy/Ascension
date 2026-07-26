package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SoundFeature(
        Identifier sound,
        ScaledValue volume,
        ScaledValue pitch
) implements SkillExecutionFeature {
    public static final MapCodec<SoundFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(SoundFeature::sound),
            ScaledValue.CODEC.codec().optionalFieldOf("volume", ScaledValue.constant(1.0D))
                    .forGetter(SoundFeature::volume),
            ScaledValue.CODEC.codec().optionalFieldOf("pitch", ScaledValue.constant(1.0D))
                    .forGetter(SoundFeature::pitch)
    ).apply(instance, SoundFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SOUND.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(sound);
        if (event == null) {
            return;
        }
        float resolvedVolume = (float) Math.clamp(volume.resolve(context.scaledValueContext()), 0.0D, 4.0D);
        float resolvedPitch = (float) Math.clamp(pitch.resolve(context.scaledValueContext()), 0.01D, 4.0D);
        context.level().playSeededSound(
                null,
                context.position().x,
                context.position().y,
                context.position().z,
                event,
                SoundSource.PLAYERS,
                resolvedVolume,
                resolvedPitch,
                context.level().getRandom().nextLong()
        );
    }
}
