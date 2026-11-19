package net.zic.ascension.api.ascension.core.skill.particle_field;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.value.HexColorCodec;

public record ParticleFieldColour(int rgb) {
    public static final Codec<ParticleFieldColour> CODEC = HexColorCodec.RGB_CODEC
            .xmap(ParticleFieldColour::new, ParticleFieldColour::rgb);

    public ParticleFieldColour {
        rgb &= 0xFFFFFF;
    }

    public int red() {
        return rgb >> 16 & 0xFF;
    }

    public int green() {
        return rgb >> 8 & 0xFF;
    }

    public int blue() {
        return rgb & 0xFF;
    }
}
