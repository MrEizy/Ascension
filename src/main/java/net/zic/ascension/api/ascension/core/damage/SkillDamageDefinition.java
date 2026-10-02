package net.zic.ascension.api.ascension.core.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record SkillDamageDefinition(
        ScaledValue base,
        ScaledValue weapon,
        Map<Identifier, ScaledValue> stats,
        Map<Identifier, ScaledValue> attributes,
        Optional<Double> minimum,
        Optional<Double> maximum
) {
    private static final Codec<Map<Identifier, ScaledValue>> SCALE_MAP = Codec.unboundedMap(
            Identifier.CODEC,
            ScaledValue.COMPACT_CODEC
    );

    public static final MapCodec<SkillDamageDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.optionalFieldOf("base", ScaledValue.constant(0.0D)).forGetter(SkillDamageDefinition::base),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("weapon", ScaledValue.constant(0.0D)).forGetter(SkillDamageDefinition::weapon),
            SCALE_MAP.optionalFieldOf("stats", Map.of()).forGetter(SkillDamageDefinition::stats),
            SCALE_MAP.optionalFieldOf("attributes", Map.of()).forGetter(SkillDamageDefinition::attributes),
            Codec.DOUBLE.optionalFieldOf("minimum").forGetter(SkillDamageDefinition::minimum),
            Codec.DOUBLE.optionalFieldOf("maximum").forGetter(SkillDamageDefinition::maximum)
    ).apply(instance, SkillDamageDefinition::new));

    public SkillDamageDefinition {
        base = base == null ? ScaledValue.constant(0.0D) : base;
        weapon = weapon == null ? ScaledValue.constant(0.0D) : weapon;
        stats = stats == null ? Map.of() : Map.copyOf(stats);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        minimum = minimum == null ? Optional.empty() : minimum;
        maximum = maximum == null ? Optional.empty() : maximum;
    }

    public static SkillDamageDefinition base(double damage) {
        return new SkillDamageDefinition(
                ScaledValue.constant(damage),
                ScaledValue.constant(0.0D),
                Map.of(),
                Map.of(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public AscensionDamageProfile resolve(ScaledValue.Context context) {
        return new AscensionDamageProfile(
                nonNegative(base.resolve(context)),
                nonNegative(weapon.resolve(context)),
                resolveScales(stats, context),
                resolveScales(attributes, context),
                minimum,
                maximum
        );
    }

    private static Map<Identifier, Double> resolveScales(
            Map<Identifier, ScaledValue> values,
            ScaledValue.Context context
    ) {
        if (values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<Identifier, Double> resolved = new LinkedHashMap<>();
        values.forEach((id, value) -> {
            double scale = value == null ? 0.0D : value.resolve(context);
            if (id != null && Double.isFinite(scale) && Math.abs(scale) > 1.0E-12D) {
                resolved.put(id, scale);
            }
        });
        return Map.copyOf(resolved);
    }

    private static double nonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }
}
