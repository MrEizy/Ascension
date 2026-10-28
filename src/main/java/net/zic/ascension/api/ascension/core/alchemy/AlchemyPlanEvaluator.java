package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementTechnique.Stage;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalComposition;

public final class AlchemyPlanEvaluator {
    private AlchemyPlanEvaluator() {
    }

    public static AlchemyRefinementTechnique technique(AlchemyRefinementPlan plan, Stage stage, RegistryAccess access) {
        Registry<AlchemyRefinementTechnique> registry = AlchemyRefinementRegistries.TECHNIQUES.get(access);
        AlchemyRefinementTechnique selected = registry.getValue(plan.selected(stage));
        if (selected != null && selected.stage() == stage) return selected;
        AlchemyRefinementTechnique fallback = registry.getValue(AlchemyRefinementPlan.DEFAULT.selected(stage));
        return fallback != null && fallback.stage() == stage ? fallback : neutral(stage);
    }

    private static AlchemyRefinementTechnique neutral(Stage stage) {
        return new AlchemyRefinementTechnique(stage, 1.0D, 2.5D, 1.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static AlchemySubstance extract(AlchemyMaterial material, AlchemyContext context,
                                           AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyRefinementTechnique extraction = technique(plan, Stage.EXTRACTION, access);
        AlchemyRefinementTechnique heating = technique(plan, Stage.HEATING, access);
        AlchemyProcessHooks.ProcessSettings settings = new AlchemyProcessHooks.ProcessSettings(
                heating.heat(), heating.control(), 3.0D, 8.0D);
        AlchemySubstance base = AlchemyRefinementResolver.refine(material, context, settings).substance();
        return new AlchemySubstance(base.properties(), base.affinities(), base.rankTier(),
                base.amplifier() * extraction.yield() * heating.yield(),
                Mth.clamp(base.purity() + extraction.purityDelta() + heating.purityDelta(), 0.0D, 1.0D),
                Math.max(0.0D, base.instability() + extraction.instabilityDelta() + heating.instabilityDelta()));
    }

    public static AlchemyProcessHooks.ProcessSettings reactions(AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyRefinementTechnique heat = technique(plan, Stage.HEATING, access);
        AlchemyRefinementTechnique stabilize = technique(plan, Stage.STABILIZATION, access);
        double safe = Math.max(0.1D, 3.0D + heat.safeEnergyBonus() + stabilize.safeEnergyBonus());
        double explosion = Math.max(safe + 0.1D, 8.0D + heat.explosionEnergyBonus() + stabilize.explosionEnergyBonus());
        return new AlchemyProcessHooks.ProcessSettings(heat.heat(), heat.control(), safe, explosion);
    }

    public static MedicinalComposition stabilize(MedicinalComposition composition, int count,
                                                  AlchemyRefinementPlan plan, RegistryAccess access) {
        if (composition.isEmpty()) return composition;
        AlchemyRefinementTechnique method = technique(plan, Stage.STABILIZATION, access);
        AlchemySubstance base = composition.substance();
        AlchemySubstance result = new AlchemySubstance(base.properties(), base.affinities(), base.rankTier(),
                base.amplifier() * method.yield(),
                Mth.clamp(base.purity() + method.purityDelta() * Math.max(1, count), 0.0D, 1.0D),
                Math.max(0.0D, base.instability() + method.instabilityDelta() * Math.max(1, count)));
        return new MedicinalComposition(result, composition.products(),
                Math.max(0.0D, composition.toxicity() + method.toxicityDelta() * Math.max(1, count)));
    }

    public static double finalQuality(double ingredientQuality, AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyRefinementTechnique method = technique(plan, Stage.CONDENSATION, access);
        return Mth.clamp(ingredientQuality + method.qualityBonus(), 0.0D, 1.0D);
    }
}
