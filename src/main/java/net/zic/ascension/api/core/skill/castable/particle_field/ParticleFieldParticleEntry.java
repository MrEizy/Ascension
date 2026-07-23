package net.zic.ascension.api.core.skill.castable.particle_field;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ParticleFieldParticleEntry(ParticleFieldParticleKind type, int weight) {
    private static final Codec<ParticleFieldParticleEntry> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ParticleFieldParticleKind.CODEC.fieldOf("type").forGetter(ParticleFieldParticleEntry::type),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(ParticleFieldParticleEntry::weight)
            ).apply(instance, ParticleFieldParticleEntry::new)
    );

    public static final Codec<ParticleFieldParticleEntry> CODEC = Codec.either(
            ParticleFieldParticleKind.CODEC,
            OBJECT_CODEC
    ).xmap(
            either -> either.map(kind -> new ParticleFieldParticleEntry(kind, 1), entry -> entry),
            entry -> entry.weight == 1 ? Either.left(entry.type) : Either.right(entry)
    );

    public ParticleFieldParticleEntry {
        if (type == null) {
            type = ParticleFieldParticleKind.MOTE;
        }
        weight = Math.max(1, weight);
    }
}
