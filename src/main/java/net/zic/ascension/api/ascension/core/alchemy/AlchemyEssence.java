package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record AlchemyEssence(
        Map<Identifier, Double> properties,
        Map<Identifier, Double> affinities,
        int rankTier,
        double amplifier
) {
    public static final Codec<AlchemyEssence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(AlchemyEssence::properties),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(AlchemyEssence::affinities),
            Codec.INT.optionalFieldOf("rank_tier", 0).forGetter(AlchemyEssence::rankTier),
            Codec.DOUBLE.optionalFieldOf("amplifier", 1.0D).forGetter(AlchemyEssence::amplifier)
    ).apply(instance, AlchemyEssence::new));

    public static final AlchemyEssence EMPTY = new AlchemyEssence(Map.of(), Map.of(), 0, 1.0D);

    public AlchemyEssence {
        properties = sanitize(properties);
        affinities = sanitize(affinities);
        rankTier = Mth.clamp(rankTier, 0, 5);
        amplifier = finiteNonNegative(amplifier, 1.0D);
    }

    public boolean isEmpty() {
        return properties.isEmpty() && affinities.isEmpty();
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
