package net.thejadeproject.ascension.particle.aura;

import java.awt.*;

public final class AuraColourHelper {
    private AuraColourHelper() {
    }

    public static AuraParticleColor vary(
            AuraParticleColor base,
            float hueVariation,
            float saturationVariation,
            float brightnessVariation
    ) {
        float[] hsb = Color.RGBtoHSB(
                (int) (base.red() * 255),
                (int) (base.green() * 255),
                (int) (base.blue() * 255),
                null
        );

        float hue = clamp01(
                hsb[0] + randomSigned(hueVariation)
        );

        float saturation = clamp01(
                hsb[1] + randomSigned(saturationVariation)
        );

        float brightness = clamp01(
                hsb[2] + randomSigned(brightnessVariation)
        );

        Color varied = Color.getHSBColor(hue, saturation, brightness);

        return new AuraParticleColor(
                varied.getRed() / 255f,
                varied.getGreen() / 255f,
                varied.getBlue() / 255f
        );
    }

    private static float randomSigned(float amount) {
        return (float) ((Math.random() * 2.0 - 1.0) * amount);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public static AuraParticleColor pulse(
            AuraParticleColor base,
            int ticksElapsed,
            float speed,
            float strength
    ) {
        float wave = (float) Math.sin(ticksElapsed * speed);
        float multiplier = 1.0f + wave * strength;

        return new AuraParticleColor(
                clamp01(base.red() * multiplier),
                clamp01(base.green() * multiplier),
                clamp01(base.blue() * multiplier)
        );
    }
}