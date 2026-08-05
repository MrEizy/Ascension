package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.castable.CastSoundDefinition;

import java.util.List;

public final class CastSoundPlayer {
    private CastSoundPlayer() {
    }

    public static void playPeriodic(LivingEntity caster, List<CastSoundDefinition> sounds, int ticksElapsed) {
        if (!(caster.level() instanceof ServerLevel level) || sounds == null || sounds.isEmpty()) {
            return;
        }

        for (CastSoundDefinition definition : sounds) {
            if (definition == null || !definition.shouldPlay(ticksElapsed)) {
                continue;
            }

            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(definition.sound());
            if (sound == null) {
                continue;
            }

            float pitchOffset = definition.pitchVariance() == 0.0F ? 0.0F : (level.getRandom().nextFloat() * 2.0F - 1.0F) * definition.pitchVariance();
            float resolvedPitch = Math.clamp(definition.pitch() + pitchOffset, 0.01F, 4.0F);

            level.playSeededSound(
                    null,
                    caster.getX(),
                    caster.getY(),
                    caster.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    definition.volume(),
                    resolvedPitch,
                    level.getRandom().nextLong()
            );
        }
    }
}
