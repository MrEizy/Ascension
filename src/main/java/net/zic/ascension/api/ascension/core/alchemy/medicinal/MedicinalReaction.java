package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;

import java.util.Map;
import java.util.Optional;

/**
 * Describes a bounded, ordered reaction between an existing mixture and an incoming essence.
 * Required reagents are consumed once per insertion; products may react on later insertions.
 */
public record MedicinalReaction(
        Reagents existing,
        Reagents incoming,
        Reagents output,
        Map<Identifier, Double> products,
        Optional<Relationship> relationship,
        int priority,
        double purityDelta,
        double instabilityDelta,
        double toxicityDelta,
        double energyDelta
) {
    public static final Codec<MedicinalReaction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Reagents.CODEC.fieldOf("existing").forGetter(MedicinalReaction::existing),
            Reagents.CODEC.fieldOf("incoming").forGetter(MedicinalReaction::incoming),
            Reagents.CODEC.optionalFieldOf("output", Reagents.EMPTY).forGetter(MedicinalReaction::output),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("products", Map.of()).forGetter(MedicinalReaction::products),
            Relationship.CODEC.optionalFieldOf("relationship").forGetter(MedicinalReaction::relationship),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MedicinalReaction::priority),
            Codec.DOUBLE.optionalFieldOf("purity_delta", 0.0D).forGetter(MedicinalReaction::purityDelta),
            Codec.DOUBLE.optionalFieldOf("instability_delta", 0.0D).forGetter(MedicinalReaction::instabilityDelta),
            Codec.DOUBLE.optionalFieldOf("toxicity_delta", 0.0D).forGetter(MedicinalReaction::toxicityDelta),
            Codec.DOUBLE.optionalFieldOf("energy_delta", 0.0D).forGetter(MedicinalReaction::energyDelta)
    ).apply(instance, MedicinalReaction::new));

    public MedicinalReaction {
        existing = existing == null ? Reagents.EMPTY : existing;
        incoming = incoming == null ? Reagents.EMPTY : incoming;
        output = output == null ? Reagents.EMPTY : output;
        products = MedicinalComposition.bounded(products);
        relationship = relationship == null ? Optional.empty() : relationship;
        priority = Mth.clamp(priority, -1000, 1000);
        purityDelta = boundSigned(purityDelta);
        instabilityDelta = boundSigned(instabilityDelta);
        toxicityDelta = boundSigned(toxicityDelta);
        energyDelta = boundSigned(energyDelta);
        if (existing.isEmpty() || incoming.isEmpty()) {
            throw new IllegalArgumentException("Medicinal reactions require reagents from both the existing and incoming essence");
        }
    }

    public record Reagents(Map<Identifier, Double> properties, Map<Identifier, Double> affinities,
                           Map<Identifier, Double> products) {
        public static final Reagents EMPTY = new Reagents(Map.of(), Map.of(), Map.of());
        public static final Codec<Reagents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(Reagents::properties),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(Reagents::affinities),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("products", Map.of()).forGetter(Reagents::products)
        ).apply(instance, Reagents::new));

        public Reagents {
            properties = MedicinalComposition.bounded(properties);
            affinities = MedicinalComposition.bounded(affinities);
            products = MedicinalComposition.bounded(products);
        }

        public boolean isEmpty() {
            return properties.isEmpty() && affinities.isEmpty() && products.isEmpty();
        }
    }

    public record Relationship(Identifier source, Identifier target, PathInteractionType type) {
        private static final Codec<PathInteractionType> TYPE_CODEC = Codec.STRING.comapFlatMap(
                name -> {
                    try {
                        return DataResult.success(PathInteractionType.valueOf(name.toUpperCase(java.util.Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown path interaction type: " + name);
                    }
                },
                type -> type.name().toLowerCase(java.util.Locale.ROOT)
        );
        public static final Codec<Relationship> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("source").forGetter(Relationship::source),
                Identifier.CODEC.fieldOf("target").forGetter(Relationship::target),
                TYPE_CODEC.fieldOf("type").forGetter(Relationship::type)
        ).apply(instance, Relationship::new));
    }

    private static double boundSigned(double value) {
        return Double.isFinite(value) ? Mth.clamp(value, -4.0D, 4.0D) : 0.0D;
    }
}
