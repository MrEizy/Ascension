package net.zic.ascension.api.ascension.core.skill.particle_field;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public record ParticleFieldIntRange(int min, int max) {
    private static final Codec<ParticleFieldIntRange> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("min").forGetter(ParticleFieldIntRange::min),
                    Codec.INT.fieldOf("max").forGetter(ParticleFieldIntRange::max)
            ).apply(instance, ParticleFieldIntRange::new)
    );

    public static final Codec<ParticleFieldIntRange> CODEC = Codec.either(Codec.INT, OBJECT_CODEC)
            .xmap(either -> either.map(ParticleFieldIntRange::fixed, range -> range),
            range -> range.min == range.max ? Either.left(range.min) : Either.right(range)
    );

    public ParticleFieldIntRange {
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
    }

    public static ParticleFieldIntRange fixed(int value) {
        return new ParticleFieldIntRange(value, value);
    }

    public int random(RandomSource random) {
        return min == max ? min : random.nextIntBetweenInclusive(min, max);
    }
}
