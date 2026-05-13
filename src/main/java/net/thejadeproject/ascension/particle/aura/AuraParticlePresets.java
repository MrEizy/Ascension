package net.thejadeproject.ascension.particle.aura;

import net.minecraft.core.particles.ParticleOptions;
import net.thejadeproject.ascension.particle.ModParticles;

public final class AuraParticlePresets {
    private AuraParticlePresets() {
    }

    public static ParticleOptions customSmoke(AuraParticleColor color, float scale, int lifetime) {
        return AuraParticleOptions.of(
                ModParticles.AURA_SMOKE.get(),
                color,
                scale,
                lifetime
        );
    }

    public static ParticleOptions customSpark(AuraParticleColor color, float scale, int lifetime) {
        return AuraParticleOptions.of(
                ModParticles.AURA_SPARK.get(),
                color,
                scale,
                lifetime
        );
    }

    public static ParticleOptions customFlame(AuraParticleColor color, float scale, int lifetime) {
        return AuraParticleOptions.of(
                ModParticles.AURA_FLAME.get(),
                color,
                scale,
                lifetime
        );
    }

}