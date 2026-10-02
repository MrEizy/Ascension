package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record AlchemySubstance(
        Map<Identifier, Double> properties,
        Map<Identifier, Double> affinities,
        int rankTier,
        double amplifier,
        double purity,
        double instability
) {
    public static final Codec<AlchemySubstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(AlchemySubstance::properties),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(AlchemySubstance::affinities),
            Codec.INT.optionalFieldOf("rank_tier", 0).forGetter(AlchemySubstance::rankTier),
            Codec.DOUBLE.optionalFieldOf("amplifier", 1.0D).forGetter(AlchemySubstance::amplifier),
            Codec.DOUBLE.optionalFieldOf("purity", 1.0D).forGetter(AlchemySubstance::purity),
            Codec.DOUBLE.optionalFieldOf("instability", 0.0D).forGetter(AlchemySubstance::instability)
    ).apply(instance, AlchemySubstance::new));

    public static final AlchemySubstance EMPTY = new AlchemySubstance(Map.of(), Map.of(), 0, 1.0D, 1.0D, 0.0D);

    public AlchemySubstance {
        properties = sanitize(properties);
        affinities = sanitize(affinities);
        rankTier = Mth.clamp(rankTier, 0, 5);
        amplifier = finiteNonNegative(amplifier, 1.0D);
        purity = Mth.clamp(Double.isFinite(purity) ? purity : 1.0D, 0.0D, 1.0D);
        instability = finiteNonNegative(instability, 0.0D);
    }

    public boolean isEmpty() {
        return properties.isEmpty() && affinities.isEmpty();
    }

    public AlchemyEssence essence() {
        return isEmpty() ? AlchemyEssence.EMPTY : new AlchemyEssence(properties, affinities, rankTier, amplifier);
    }

    private static Map<Identifier, Double> sanitize(Map<Identifier, Double> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> sanitized = new LinkedHashMap<>();
        values.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value) && value > 1.0E-12D) {
                sanitized.put(id, value);
            }
        });
        return Collections.unmodifiableMap(sanitized);
    }

    private static double finiteNonNegative(double value, double fallback) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : fallback;
    }
}
