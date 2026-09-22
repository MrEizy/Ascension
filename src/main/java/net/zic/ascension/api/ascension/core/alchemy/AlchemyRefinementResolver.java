package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.util.Mth;

public final class AlchemyRefinementResolver {
    private static final double EPSILON = 1.0E-12D;

    private AlchemyRefinementResolver() {
    }

    public static RefinementResult refine(AlchemyMaterial material, AlchemyContext context) {
        if (material == null || material.isEmpty()) {
            return RefinementResult.EMPTY;
        }

        AlchemyProcessHooks.ProcessSettings settings = AlchemyProcessHooks.resolve(context == null ? AlchemyContext.EMPTY : context);
        double capacity = Math.max(EPSILON, settings.heat() * settings.refinementControl());
        double difficulty = Math.max(0.0D, material.refinementDifficulty());
        double strain = difficulty <= capacity ? 0.0D : (difficulty - capacity) / capacity;
        double normalizedStrain = Mth.clamp(strain, 0.0D, 2.0D);

        double amplifier = material.essence().amplifier() * (1.0D - Math.min(0.25D, normalizedStrain * 0.08D));
        double purity = material.purity() * (1.0D - Math.min(0.35D, normalizedStrain * 0.12D));
        double instability = material.instability() + normalizedStrain * 0.2D;

        AlchemySubstance substance = new AlchemySubstance(
                material.essence().properties(),
                material.essence().affinities(),
                material.essence().rankTier(),
                amplifier,
                purity,
                instability
        );
        return new RefinementResult(substance, normalizedStrain);
    }

    public record RefinementResult(AlchemySubstance substance, double strain) {
        public static final RefinementResult EMPTY = new RefinementResult(AlchemySubstance.EMPTY, 0.0D);

        public RefinementResult {
            substance = substance == null ? AlchemySubstance.EMPTY : substance;
            strain = Double.isFinite(strain) ? Math.max(0.0D, strain) : 0.0D;
        }

        public boolean isEmpty() {
            return substance.isEmpty();
        }
    }
}
