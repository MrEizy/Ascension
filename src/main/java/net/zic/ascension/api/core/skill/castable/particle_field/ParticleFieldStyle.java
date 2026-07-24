package net.zic.ascension.api.core.skill.castable.particle_field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ParticleFieldStyle {
    RISING("rising"),
    INWARD_FLOW("inward_flow"),
    SPIRAL("spiral"),
    MERIDIAN_FLOW("meridian_flow"),
    GATHERING_RING("gathering_ring");

    public static final Codec<ParticleFieldStyle> CODEC =
            Codec.STRING.comapFlatMap(ParticleFieldStyle::decode, ParticleFieldStyle::serializedName);

    private final String serializedName;

    ParticleFieldStyle(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<ParticleFieldStyle> decode(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        for (ParticleFieldStyle style : values()) {
            if (style.serializedName.equals(normalized)) {
                return DataResult.success(style);
            }
        }
        return DataResult.error(() -> "Unknown particle field style: " + value);
    }
}
