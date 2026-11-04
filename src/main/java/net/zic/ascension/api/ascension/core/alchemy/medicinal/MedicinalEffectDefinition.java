package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Defines an entire medicinal effect in one datapack entry: expression, execution and pill names.
 * The registry entry ID is the effect ID, so no duplicate identifier fields are needed.
 */
public record MedicinalEffectDefinition(
        MedicinalReaction.Reagents requirements,
        double minimumPurity,
        double maximumInstability,
        double minimumToxicity,
        double potencyPerUnit,
        int durationTicks,
        boolean adverse,
        int priority,
        MedicinalEffectAction application,
        List<String> names
) {
    public static final Codec<MedicinalEffectDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MedicinalReaction.Reagents.CODEC.optionalFieldOf("requirements", MedicinalReaction.Reagents.EMPTY).forGetter(MedicinalEffectDefinition::requirements),
            Codec.DOUBLE.optionalFieldOf("minimum_purity", 0.0D).forGetter(MedicinalEffectDefinition::minimumPurity),
            Codec.DOUBLE.optionalFieldOf("maximum_instability", 4096.0D).forGetter(MedicinalEffectDefinition::maximumInstability),
            Codec.DOUBLE.optionalFieldOf("minimum_toxicity", 0.0D).forGetter(MedicinalEffectDefinition::minimumToxicity),
            Codec.DOUBLE.optionalFieldOf("potency_per_unit", 1.0D).forGetter(MedicinalEffectDefinition::potencyPerUnit),
            Codec.INT.optionalFieldOf("duration_ticks", 0).forGetter(MedicinalEffectDefinition::durationTicks),
            Codec.BOOL.optionalFieldOf("adverse", false).forGetter(MedicinalEffectDefinition::adverse),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MedicinalEffectDefinition::priority),
            MedicinalEffectAction.CODEC.fieldOf("application").forGetter(MedicinalEffectDefinition::application),
            Codec.STRING.listOf().optionalFieldOf("names", List.of()).forGetter(MedicinalEffectDefinition::names)
    ).apply(instance, MedicinalEffectDefinition::new));

    public MedicinalEffectDefinition {
        requirements = requirements == null ? MedicinalReaction.Reagents.EMPTY : requirements;
        minimumPurity = finiteRange(minimumPurity, 0.0D, 1.0D, 0.0D);
        maximumInstability = finiteRange(maximumInstability, 0.0D, 4096.0D, 4096.0D);
        minimumToxicity = finiteRange(minimumToxicity, 0.0D, 4096.0D, 0.0D);
        potencyPerUnit = finiteRange(potencyPerUnit, 0.0D, 64.0D, 1.0D);
        durationTicks = Mth.clamp(durationTicks, 0, 72000);
        priority = Mth.clamp(priority, -1000, 1000);
        if (application == null) throw new IllegalArgumentException("Missing medicinal effect application");
        names = names == null ? List.of() : names.stream().filter(name -> name != null && !name.isBlank())
                .limit(16).toList();
    }

    private static double finiteRange(double number, double min, double max, double fallback) {
        return Double.isFinite(number) ? Mth.clamp(number, min, max) : fallback;
    }
}
