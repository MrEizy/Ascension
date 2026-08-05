package net.zic.ascension.api.ascension.core.skill.particle_field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ParticleFieldParticleKind {
    SPARK("spark"),
    WISP("wisp"),
    BLOB("blob"),
    MOTE("mote"),
    PETAL("petal"),
    RUNE("rune"),
    THREAD("thread");

    public static final Codec<ParticleFieldParticleKind> CODEC =
            Codec.STRING.comapFlatMap(ParticleFieldParticleKind::decode, ParticleFieldParticleKind::serializedName);

    private final String serializedName;

    ParticleFieldParticleKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<ParticleFieldParticleKind> decode(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        for (ParticleFieldParticleKind kind : values()) {
            if (kind.serializedName.equals(normalized)) {
                return DataResult.success(kind);
            }
        }
        return DataResult.error(() -> "Unknown particle field particle kind: " + value);
    }
}
