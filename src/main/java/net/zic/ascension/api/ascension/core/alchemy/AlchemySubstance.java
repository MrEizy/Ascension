package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record AlchemySubstance(
        Map<Identifier, Double> properties,
        Map<Identifier, Double> affinities,
        int majorRealm,
        int minorRealm,
        double potency,
        double purity,
        double instability
) {
    public static final Codec<AlchemySubstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("properties", Map.of()).forGetter(AlchemySubstance::properties),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("affinities", Map.of()).forGetter(AlchemySubstance::affinities),
            Codec.INT.optionalFieldOf("major_realm", 0).forGetter(AlchemySubstance::majorRealm),
            Codec.INT.optionalFieldOf("minor_realm", 0).forGetter(AlchemySubstance::minorRealm),
            Codec.DOUBLE.optionalFieldOf("potency", 0.0D).forGetter(AlchemySubstance::potency),
            Codec.DOUBLE.optionalFieldOf("purity", 1.0D).forGetter(AlchemySubstance::purity),
            Codec.DOUBLE.optionalFieldOf("instability", 0.0D).forGetter(AlchemySubstance::instability)
    ).apply(instance, AlchemySubstance::new));

    public static final AlchemySubstance EMPTY = new AlchemySubstance(Map.of(), Map.of(), 0, 0, 0.0D, 1.0D, 0.0D);

    public AlchemySubstance {
        properties = sanitize(properties);
        affinities = sanitize(affinities);
        majorRealm = Math.max(0, majorRealm);
        minorRealm = Math.max(0, minorRealm);
        potency = finiteNonNegative(potency);
        purity = Mth.clamp(Double.isFinite(purity) ? purity : 1.0D, 0.0D, 1.0D);
        instability = finiteNonNegative(instability);
    }

    public static Optional<Material> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof Provider provider)) {
            return Optional.empty();
        }

        Material material = provider.alchemyMaterial(stack);
        return material == null || material.substance().isEmpty() ? Optional.empty() : Optional.of(material);
    }

    public boolean isEmpty() {
        return properties.isEmpty() && affinities.isEmpty() && potency <= 1.0E-12D;
    }

    public int realmScore() {
        return majorRealm * 10 + minorRealm;
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

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }

    public interface Provider {
        Material alchemyMaterial(ItemStack stack);
    }

    public record Material(AlchemySubstance substance, double refinementDifficulty) {
        public Material {
            substance = substance == null ? EMPTY : substance;
            refinementDifficulty = finiteNonNegative(refinementDifficulty);
        }
    }
}
