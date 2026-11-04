package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.Registry;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.zic.ascension.impl.core.technique.AlchemyTechnique.Stage;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalComposition;

public final class AlchemyPlanEvaluator {
    private AlchemyPlanEvaluator() {
    }

    public static AlchemyTechnique.Refinement technique(AlchemyRefinementPlan plan, Stage stage, RegistryAccess access) {
        Registry<Technique<?>> registry = CoreRegistries.TECHNIQUE_REGISTRY.get(access);
        Technique<?> selected = registry.getValue(plan.selected(stage));
        if (selected instanceof AlchemyTechnique alchemy && alchemy.stage() == stage) return alchemy.refinement();
        Technique<?> fallback = registry.getValue(AlchemyRefinementPlan.DEFAULT.selected(stage));
        if (fallback instanceof AlchemyTechnique alchemy && alchemy.stage() == stage) return alchemy.refinement();
        return AlchemyTechnique.Refinement.neutral(stage);
    }

    public static AlchemySubstance extract(AlchemyMaterial material, AlchemyContext context,
                                           AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyTechnique.Refinement extraction = technique(plan, Stage.EXTRACTION, access);
        AlchemyTechnique.Refinement heating = technique(plan, Stage.HEATING, access);
        AlchemyProcessHooks.ProcessSettings settings = new AlchemyProcessHooks.ProcessSettings(
                heating.heat(), heating.control(), 3.0D, 8.0D);
        AlchemySubstance base = AlchemyRefinementResolver.refine(material, context, settings).substance();
        return new AlchemySubstance(base.properties(), base.affinities(), base.rankTier(),
                base.amplifier() * extraction.yield() * heating.yield(),
                Mth.clamp(base.purity() + extraction.purityDelta() + heating.purityDelta(), 0.0D, 1.0D),
                Math.max(0.0D, base.instability() + extraction.instabilityDelta() + heating.instabilityDelta()));
    }

    public static AlchemyProcessHooks.ProcessSettings reactions(AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyTechnique.Refinement heat = technique(plan, Stage.HEATING, access);
        AlchemyTechnique.Refinement stabilize = technique(plan, Stage.STABILIZATION, access);
        double safe = Math.max(0.1D, 3.0D + heat.safeEnergyBonus() + stabilize.safeEnergyBonus());
        double explosion = Math.max(safe + 0.1D, 8.0D + heat.explosionEnergyBonus() + stabilize.explosionEnergyBonus());
        return new AlchemyProcessHooks.ProcessSettings(heat.heat(), heat.control(), safe, explosion);
    }

    public static MedicinalComposition stabilize(MedicinalComposition composition, int count,
                                                  AlchemyRefinementPlan plan, RegistryAccess access) {
        if (composition.isEmpty()) return composition;
        AlchemyTechnique.Refinement method = technique(plan, Stage.STABILIZATION, access);
        AlchemySubstance base = composition.substance();
        AlchemySubstance result = new AlchemySubstance(base.properties(), base.affinities(), base.rankTier(),
                base.amplifier() * method.yield(),
                Mth.clamp(base.purity() + method.purityDelta() * Math.max(1, count), 0.0D, 1.0D),
                Math.max(0.0D, base.instability() + method.instabilityDelta() * Math.max(1, count)));
        return new MedicinalComposition(result, composition.products(),
                Math.max(0.0D, composition.toxicity() + method.toxicityDelta() * Math.max(1, count)));
    }

    public static double finalQuality(double ingredientQuality, AlchemyRefinementPlan plan, RegistryAccess access) {
        AlchemyTechnique.Refinement method = technique(plan, Stage.CONDENSATION, access);
        return Mth.clamp(ingredientQuality + method.qualityBonus(), 0.0D, 1.0D);
    }
}
