package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;

public record SoundReleaseFeature(
        Identifier sound,
        ScaledValue volume,
        ScaledValue pitch
) implements HeldCastReleaseFeature {
    public static final MapCodec<SoundReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(SoundReleaseFeature::sound),
            ScaledValue.CODEC.codec().optionalFieldOf("volume", ScaledValue.constant(1.0D))
                    .forGetter(SoundReleaseFeature::volume),
            ScaledValue.CODEC.codec().optionalFieldOf("pitch", ScaledValue.constant(1.0D))
                    .forGetter(SoundReleaseFeature::pitch)
    ).apply(instance, SoundReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.SOUND.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(sound);
        if (event == null) {
            return;
        }
        float resolvedVolume = (float) Math.clamp(volume.resolve(context.scaledValueContext()), 0.0D, 4.0D);
        float resolvedPitch = (float) Math.clamp(pitch.resolve(context.scaledValueContext()), 0.01D, 4.0D);
        context.execution().level().playSeededSound(
                null,
                context.position().x,
                context.position().y,
                context.position().z,
                event,
                SoundSource.PLAYERS,
                resolvedVolume,
                resolvedPitch,
                context.execution().level().getRandom().nextLong()
        );
    }
}
