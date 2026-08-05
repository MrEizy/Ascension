package net.zic.ascension.api.ascension.core.skill.particle_field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public record ParticleFieldColour(int rgb) {
    public static final Codec<ParticleFieldColour> CODEC =
            Codec.STRING.comapFlatMap(ParticleFieldColour::decode, ParticleFieldColour::encode);

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

    private static DataResult<ParticleFieldColour> decode(String value) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (normalized.length() != 6) {
            return DataResult.error(() -> "Particle field colours must use RRGGBB or #RRGGBB: " + value);
        }
        try {
            return DataResult.success(new ParticleFieldColour(Integer.parseInt(normalized, 16)));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid particle field colour: " + value);
        }
    }

    private static String encode(ParticleFieldColour colour) {
        return String.format(Locale.ROOT, "#%06X", colour.rgb);
    }
}
