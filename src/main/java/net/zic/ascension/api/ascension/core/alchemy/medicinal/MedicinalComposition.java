package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record MedicinalComposition(AlchemySubstance substance, Map<Identifier, Double> products, double toxicity) {
    public static final Codec<MedicinalComposition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AlchemySubstance.CODEC.fieldOf("substance").forGetter(MedicinalComposition::substance),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("products", Map.of()).forGetter(MedicinalComposition::products),
            Codec.DOUBLE.optionalFieldOf("toxicity", 0.0D).forGetter(MedicinalComposition::toxicity)
    ).apply(instance, MedicinalComposition::new));

    public static final MedicinalComposition EMPTY = new MedicinalComposition(AlchemySubstance.EMPTY, Map.of(), 0.0D);
    public static final double MAX_CONCENTRATION = 4096.0D;

    public MedicinalComposition {
        substance = substance == null ? AlchemySubstance.EMPTY : substance;
        products = bounded(products);
        toxicity = Double.isFinite(toxicity) ? Mth.clamp(toxicity, 0.0D, MAX_CONCENTRATION) : 0.0D;
    }

    public static MedicinalComposition of(AlchemySubstance substance) {
        return substance == null || substance.isEmpty() ? EMPTY : new MedicinalComposition(substance, Map.of(), 0.0D);
    }

    public boolean isEmpty() {
        return substance.isEmpty() && products.isEmpty();
    }

    public static Map<Identifier, Double> bounded(Map<Identifier, Double> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> cleaned = new LinkedHashMap<>();
        raw.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(Identifier::toString)))
                .limit(64).forEach(entry -> {
                    double amount = entry.getValue();
                    if (Double.isFinite(amount) && amount > 1.0E-9D) {
                        cleaned.put(entry.getKey(), Math.min(MAX_CONCENTRATION, amount));
                    }
                });
        return Collections.unmodifiableMap(cleaned);
    }
}
