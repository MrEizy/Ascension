package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record AlchemyFormula(
        Identifier output,
        Requirements requirements,
        Optional<Consumption> consumption,
        int priority,
        int baseYield,
        int maximumYield,
        double amplifierPerBonus
) {
    public static final Codec<AlchemyFormula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("output").forGetter(AlchemyFormula::output),
            Requirements.CODEC.fieldOf("requirements").forGetter(AlchemyFormula::requirements),
            Consumption.CODEC.optionalFieldOf("consumption").forGetter(AlchemyFormula::consumption),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(AlchemyFormula::priority),
            Codec.intRange(1, 64).optionalFieldOf("base_yield", 1).forGetter(AlchemyFormula::baseYield),
            Codec.intRange(1, 64).optionalFieldOf("maximum_yield", 1).forGetter(AlchemyFormula::maximumYield),
            Codec.DOUBLE.optionalFieldOf("amplifier_per_bonus", 1.0D).forGetter(AlchemyFormula::amplifierPerBonus)
    ).apply(instance, AlchemyFormula::new));

    public AlchemyFormula {
        output = Objects.requireNonNull(output);
        requirements = requirements == null ? Requirements.EMPTY : requirements;
        consumption = consumption == null ? Optional.empty() : consumption;
        baseYield = Math.max(1, baseYield);
        maximumYield = Math.max(baseYield, maximumYield);
        amplifierPerBonus = Double.isFinite(amplifierPerBonus) && amplifierPerBonus > 0.0D ? amplifierPerBonus : 1.0D;
        Consumption resolved = consumption.isPresent() ? consumption.get() : new Consumption(requirements.properties(), requirements.affinities());        if (resolved.isEmpty()) {
            throw new IllegalArgumentException("Alchemy formulas must consume at least one property or affinity");
        }
    }

    public Consumption resolvedConsumption() {
        return consumption.orElseGet(() -> new Consumption(requirements.properties(), requirements.affinities()));
    }

    public boolean matches(AlchemySubstance substance) {
        return requirements.matches(substance) && resolvedConsumption().fits(substance);
    }

    public double matchQuality(AlchemySubstance substance) {
        if (!matches(substance)) {
            return 0.0D;
        }
        return Mth.clamp(requirements.precision(substance) * effectivePurity(substance), 0.0D, 1.0D);
    }

    public int yield(AlchemySubstance substance) {
        if (substance == null) {
            return baseYield;
        }
        double baseline = Math.max(1.0D, requirements.minimumAmplifier());
        double excess = Math.max(0.0D, substance.amplifier() - baseline);
        int bonus = (int) Math.floor(excess / amplifierPerBonus);
        return Mth.clamp(baseYield + bonus, baseYield, maximumYield);
    }

    public AlchemySubstance consume(AlchemySubstance substance) {
        if (substance == null || substance.isEmpty()) {
            return AlchemySubstance.EMPTY;
        }
        Consumption resolved = resolvedConsumption();
        Map<Identifier, Double> properties = subtract(substance.properties(), resolved.properties());
        Map<Identifier, Double> affinities = subtract(substance.affinities(), resolved.affinities());
        if (properties.isEmpty() && affinities.isEmpty()) {
            return AlchemySubstance.EMPTY;
        }
        return new AlchemySubstance(
                properties,
                affinities,
                substance.rankTier(),
                substance.amplifier(),
                substance.purity(),
                substance.instability()
        );
    }

    public static double effectivePurity(AlchemySubstance substance) {
        if (substance == null) {
            return 0.0D;
        }
        return Mth.clamp(substance.purity() / (1.0D + substance.instability()), 0.0D, 1.0D);
    }

    private static Map<Identifier, Double> subtract(Map<Identifier, Double> source, Map<Identifier, Double> consumed) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> result = new LinkedHashMap<>(source);
        consumed.forEach((id, amount) -> {
            double remaining = result.getOrDefault(id, 0.0D) - amount;
            if (remaining <= 1.0E-9D) {
                result.remove(id);
            } else {
                result.put(id, remaining);
            }
        });
        return result;
    }

    private static Map<Identifier, Double> sanitize(Map<Identifier, Double> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> sanitized = new LinkedHashMap<>();
        values.forEach((id, value) -> {
            if (id != null && value != null && Double.isFinite(value) && value > 0.0D) {
                sanitized.put(id, value);
            }
        });
        return Collections.unmodifiableMap(sanitized);
    }

    private static boolean contains(Map<Identifier, Double> actual, Map<Identifier, Double> required) {
        for (Map.Entry<Identifier, Double> entry : required.entrySet()) {
            double value = actual.getOrDefault(entry.getKey(), 0.0D);
            if (!Double.isFinite(value) || value + 1.0E-9D < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static double mapPrecision(Map<Identifier, Double> required, Map<Identifier, Double> actual) {
        if (required.isEmpty()) {
            return 1.0D;
        }
        double requiredTotal = required.values().stream().mapToDouble(Double::doubleValue).sum();
        double actualTotal = actual.values().stream().mapToDouble(Double::doubleValue).sum();
        if (requiredTotal <= 0.0D || actualTotal <= 0.0D) {
            return 0.0D;
        }

        double distance = 0.0D;
        for (Map.Entry<Identifier, Double> entry : required.entrySet()) {
            double expectedShare = entry.getValue() / requiredTotal;
            double actualShare = actual.getOrDefault(entry.getKey(), 0.0D) / actualTotal;
            distance += Math.abs(expectedShare - actualShare);
        }
        for (Map.Entry<Identifier, Double> entry : actual.entrySet()) {
            if (!required.containsKey(entry.getKey())) {
                distance += entry.getValue() / actualTotal;
            }
        }
        return Mth.clamp(1.0D - distance * 0.5D, 0.0D, 1.0D);
    }

    public record Requirements(
            Map<Identifier, Double> properties,
            Map<Identifier, Double> affinities,
            int minimumRankTier,
            double minimumPurity,
            double minimumAmplifier
    ) {
        public static final Requirements EMPTY = new Requirements(Map.of(), Map.of(), 0, 0.0D, 0.0D);
        public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(Requirements::properties),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(Requirements::affinities),
                Codec.intRange(0, 5).optionalFieldOf("minimum_rank_tier", 0).forGetter(Requirements::minimumRankTier),
                Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("minimum_purity", 0.0D).forGetter(Requirements::minimumPurity),
                Codec.DOUBLE.optionalFieldOf("minimum_amplifier", 0.0D).forGetter(Requirements::minimumAmplifier)
        ).apply(instance, Requirements::new));

        public Requirements {
            properties = sanitize(properties);
            affinities = sanitize(affinities);
            minimumRankTier = Mth.clamp(minimumRankTier, 0, 5);
            minimumPurity = Mth.clamp(Double.isFinite(minimumPurity) ? minimumPurity : 0.0D, 0.0D, 1.0D);
            minimumAmplifier = Double.isFinite(minimumAmplifier) ? Math.max(0.0D, minimumAmplifier) : 0.0D;
        }

        public boolean matches(AlchemySubstance substance) {
            return substance != null
                    && !substance.isEmpty()
                    && substance.rankTier() >= minimumRankTier
                    && effectivePurity(substance) + 1.0E-9D >= minimumPurity
                    && substance.amplifier() + 1.0E-9D >= minimumAmplifier
                    && contains(substance.properties(), properties)
                    && contains(substance.affinities(), affinities);
        }

        public double precision(AlchemySubstance substance) {
            if (substance == null) {
                return 0.0D;
            }
            double propertyPrecision = mapPrecision(properties, substance.properties());
            double affinityPrecision = mapPrecision(affinities, substance.affinities());
            if (properties.isEmpty()) {
                return affinityPrecision;
            }
            if (affinities.isEmpty()) {
                return propertyPrecision;
            }
            return (propertyPrecision + affinityPrecision) * 0.5D;
        }
    }

    public record Consumption(Map<Identifier, Double> properties, Map<Identifier, Double> affinities) {
        public static final Codec<Consumption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(Consumption::properties),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(Consumption::affinities)
        ).apply(instance, Consumption::new));

        public Consumption {
            properties = sanitize(properties);
            affinities = sanitize(affinities);
        }

        public boolean isEmpty() {
            return properties.isEmpty() && affinities.isEmpty();
        }

        public boolean fits(AlchemySubstance substance) {
            return substance != null
                    && contains(substance.properties(), properties)
                    && contains(substance.affinities(), affinities);
        }
    }
}
