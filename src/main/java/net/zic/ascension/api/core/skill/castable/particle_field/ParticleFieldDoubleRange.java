package net.zic.ascension.api.core.skill.castable.particle_field;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public record ParticleFieldDoubleRange(double min, double max) {
    private static final Codec<ParticleFieldDoubleRange> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("min").forGetter(ParticleFieldDoubleRange::min),
                    Codec.DOUBLE.fieldOf("max").forGetter(ParticleFieldDoubleRange::max)
            ).apply(instance, ParticleFieldDoubleRange::new)
    );

    public static final Codec<ParticleFieldDoubleRange> CODEC =
            Codec.either(Codec.DOUBLE, OBJECT_CODEC).xmap(
            either -> either.map(ParticleFieldDoubleRange::fixed, range -> range),
            range -> range.min == range.max ? Either.left(range.min) : Either.right(range)
    );

    public ParticleFieldDoubleRange {
        if (!Double.isFinite(min)) {
            min = 0.0D;
        }
        if (!Double.isFinite(max)) {
            max = min;
        }
        if (max < min) {
            double swap = min;
            min = max;
            max = swap;
        }
    }

    public static ParticleFieldDoubleRange fixed(double value) {
        return new ParticleFieldDoubleRange(value, value);
    }

    public double random(RandomSource random) {
        return min == max ? min : min + random.nextDouble() * (max - min);
    }
}
