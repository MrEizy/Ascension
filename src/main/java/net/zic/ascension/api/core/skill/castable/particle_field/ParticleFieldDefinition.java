package net.zic.ascension.api.core.skill.castable.particle_field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

import java.util.List;

public record ParticleFieldDefinition(
        ParticleFieldStyle style,
        List<ParticleFieldParticleEntry> particles,
        List<ParticleFieldColour> colours,
        double density,
        ParticleFieldDoubleRange radius,
        ParticleFieldDoubleRange height,
        ParticleFieldDoubleRange speed,
        ParticleFieldDoubleRange size,
        ParticleFieldIntRange lifetime,
        boolean fullBright
) {
    public static final ParticleFieldStyle DEFAULT_STYLE = ParticleFieldStyle.INWARD_FLOW;

    public static final List<ParticleFieldParticleEntry> DEFAULT_PARTICLES = List.of(
            new ParticleFieldParticleEntry(ParticleFieldParticleKind.WISP, 6),
            new ParticleFieldParticleEntry(ParticleFieldParticleKind.MOTE, 4),
            new ParticleFieldParticleEntry(ParticleFieldParticleKind.SPARK, 1)
    );

    public static final List<ParticleFieldColour> DEFAULT_COLOURS = List.of(
            new ParticleFieldColour(0xD8F4FF)
    );

    public static final double DEFAULT_DENSITY = 20.0D;
    public static final ParticleFieldDoubleRange DEFAULT_RADIUS = new ParticleFieldDoubleRange(0.55D, 1.45D);
    public static final ParticleFieldDoubleRange DEFAULT_HEIGHT = new ParticleFieldDoubleRange(-0.05D, 1.85D);
    public static final ParticleFieldDoubleRange DEFAULT_SPEED = new ParticleFieldDoubleRange(0.045D, 0.075D);
    public static final ParticleFieldDoubleRange DEFAULT_SIZE = new ParticleFieldDoubleRange(0.04D, 0.11D);
    public static final ParticleFieldIntRange DEFAULT_LIFETIME = new ParticleFieldIntRange(18, 32);

    public static final Codec<ParticleFieldDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ParticleFieldStyle.CODEC.optionalFieldOf("style", DEFAULT_STYLE).forGetter(ParticleFieldDefinition::style),
                    ParticleFieldParticleEntry.CODEC.listOf().optionalFieldOf("particles", DEFAULT_PARTICLES).forGetter(ParticleFieldDefinition::particles),
                    ParticleFieldColour.CODEC.listOf().optionalFieldOf("colours", DEFAULT_COLOURS).forGetter(ParticleFieldDefinition::colours),
                    Codec.DOUBLE.optionalFieldOf("density", DEFAULT_DENSITY).forGetter(ParticleFieldDefinition::density),
                    ParticleFieldDoubleRange.CODEC.optionalFieldOf("radius", DEFAULT_RADIUS).forGetter(ParticleFieldDefinition::radius),
                    ParticleFieldDoubleRange.CODEC.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(ParticleFieldDefinition::height),
                    ParticleFieldDoubleRange.CODEC.optionalFieldOf("speed", DEFAULT_SPEED).forGetter(ParticleFieldDefinition::speed),
                    ParticleFieldDoubleRange.CODEC.optionalFieldOf("size", DEFAULT_SIZE).forGetter(ParticleFieldDefinition::size),
                    ParticleFieldIntRange.CODEC.optionalFieldOf("lifetime", DEFAULT_LIFETIME).forGetter(ParticleFieldDefinition::lifetime),
                    Codec.BOOL.optionalFieldOf("full_bright", true).forGetter(ParticleFieldDefinition::fullBright)
            ).apply(instance, ParticleFieldDefinition::new)
    );

    public ParticleFieldDefinition {
        if (style == null) {
            style = DEFAULT_STYLE;
        }
        particles = particles == null || particles.isEmpty() ? DEFAULT_PARTICLES : List.copyOf(particles);
        colours = colours == null || colours.isEmpty() ? DEFAULT_COLOURS : List.copyOf(colours);
        density = Double.isFinite(density) ? Math.clamp(density, 0.0D, 400.0D) : DEFAULT_DENSITY;
        radius = sanitizeNonNegative(radius, DEFAULT_RADIUS);
        height = height == null ? DEFAULT_HEIGHT : height;
        speed = sanitizeNonNegative(speed, DEFAULT_SPEED);
        size = sanitizeNonNegative(size, DEFAULT_SIZE);
        lifetime = lifetime == null ? DEFAULT_LIFETIME : new ParticleFieldIntRange(Math.max(1, lifetime.min()), Math.max(1, lifetime.max()));
    }

    public ParticleFieldParticleKind randomParticle(RandomSource random) {
        int totalWeight = particles.stream().mapToInt(ParticleFieldParticleEntry::weight).sum();
        int selection = random.nextInt(Math.max(1, totalWeight));
        for (ParticleFieldParticleEntry entry : particles) {
            selection -= entry.weight();
            if (selection < 0) {
                return entry.type();
            }
        }
        return particles.getLast().type();
    }

    private static ParticleFieldDoubleRange sanitizeNonNegative(ParticleFieldDoubleRange range, ParticleFieldDoubleRange fallback) {
        if (range == null) {
            return fallback;
        }
        return new ParticleFieldDoubleRange(Math.max(0.0D, range.min()), Math.max(0.0D, range.max()));
    }
}
