package net.zic.ascension.api.ascension.core.skill.particle_field;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record ParticleFieldParticleEntry(Identifier particle, int weight) {
    private static final Codec<ParticleFieldParticleEntry> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("particle").forGetter(ParticleFieldParticleEntry::particle),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(ParticleFieldParticleEntry::weight)
            ).apply(instance, ParticleFieldParticleEntry::new)
    );

    public static final Codec<ParticleFieldParticleEntry> CODEC =
            Codec.either(Identifier.CODEC, OBJECT_CODEC).xmap(
                    either -> either.map(id -> new ParticleFieldParticleEntry(id, 1), entry -> entry),
                    entry -> entry.weight == 1 ? Either.left(entry.particle) : Either.right(entry)
            );

    public ParticleFieldParticleEntry {
        if (particle == null) {
            particle = Identifier.fromNamespaceAndPath("ascension", "particle_field_mote");
        }
        weight = Math.max(1, weight);
    }
}
