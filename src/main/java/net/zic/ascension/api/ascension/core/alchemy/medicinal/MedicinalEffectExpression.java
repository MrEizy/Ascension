package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Defines how a medicinal composition expresses a beneficial or adverse effect.
 * Effect identifiers are logical outputs; applying effects to entities belongs to the consumption stage.
 */
public record MedicinalEffectExpression(
        Identifier effect,
        MedicinalReaction.Reagents requirements,
        double minimumPurity,
        double maximumInstability,
        double minimumToxicity,
        double potencyPerUnit,
        int durationTicks,
        boolean adverse,
        int priority
) {
    public static final Codec<MedicinalEffectExpression> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(MedicinalEffectExpression::effect),
            MedicinalReaction.Reagents.CODEC.fieldOf("requirements").forGetter(MedicinalEffectExpression::requirements),
            Codec.DOUBLE.optionalFieldOf("minimum_purity", 0.0D).forGetter(MedicinalEffectExpression::minimumPurity),
            Codec.DOUBLE.optionalFieldOf("maximum_instability", 4096.0D).forGetter(MedicinalEffectExpression::maximumInstability),
            Codec.DOUBLE.optionalFieldOf("minimum_toxicity", 0.0D).forGetter(MedicinalEffectExpression::minimumToxicity),
            Codec.DOUBLE.optionalFieldOf("potency_per_unit", 1.0D).forGetter(MedicinalEffectExpression::potencyPerUnit),
            Codec.INT.optionalFieldOf("duration_ticks", 0).forGetter(MedicinalEffectExpression::durationTicks),
            Codec.BOOL.optionalFieldOf("adverse", false).forGetter(MedicinalEffectExpression::adverse),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MedicinalEffectExpression::priority)
    ).apply(instance, MedicinalEffectExpression::new));

    public MedicinalEffectExpression {
        if (effect == null) throw new IllegalArgumentException("Effect id must exist");
        requirements = requirements == null ? MedicinalReaction.Reagents.EMPTY : requirements;
        minimumPurity = finiteRange(minimumPurity, 0.0D, 1.0D, 0.0D);
        maximumInstability = finiteRange(maximumInstability, 0.0D, 4096.0D, 4096.0D);
        minimumToxicity = finiteRange(minimumToxicity, 0.0D, 4096.0D, 0.0D);
        if (requirements.isEmpty() && minimumToxicity <= 0.0D) {
            throw new IllegalArgumentException("Effect expressions require reagents or a positive toxicity threshold");
        }
        potencyPerUnit = finiteRange(potencyPerUnit, 0.0D, 64.0D, 1.0D);
        durationTicks = Mth.clamp(durationTicks, 0, 72000);
        priority = Mth.clamp(priority, -1000, 1000);
    }

    private static double finiteRange(double number, double min, double max, double fallback) {
        return Double.isFinite(number) ? Mth.clamp(number, min, max) : fallback;
    }
}
