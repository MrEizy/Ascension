package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyProcessHooks;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates ingredient extraction, insertion-order reactions, and final medicinal expression.
 * It is currently an isolated simulation facade and does not alter existing furnace behaviour.
 */
public final class MedicinalCompositionEngine {
    private MedicinalCompositionEngine() {
    }

    public static Result simulate(List<AlchemyMaterial> ingredients, AlchemyContext context) {
        if (context == null || context.level() == null || ingredients == null || ingredients.isEmpty()) {
            return new Result(new MedicinalProfile(MedicinalComposition.EMPTY, List.of()), List.of(),
                    AlchemyMergeResolver.Outcome.STABLE, 0.0D, 0);
        }
        AlchemyProcessHooks.ProcessSettings settings = AlchemyProcessHooks.resolve(context);
        RegistryAccess access = context.level().registryAccess();
        MedicinalComposition composition = MedicinalComposition.EMPTY;
        int count = 0;
        double maximumEnergy = 0.0D;
        AlchemyMergeResolver.Outcome worst = AlchemyMergeResolver.Outcome.STABLE;
        List<Identifier> activated = new ArrayList<>();

        for (AlchemyMaterial ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) continue;
            AlchemySubstance incoming = AlchemyRefinementResolver.refine(ingredient, context).substance();
            if (incoming.isEmpty()) continue;
            MedicinalReactionResolver.Step step = MedicinalReactionResolver.add(
                    composition, count, incoming, settings.safeEnergyDelta(), settings.explosionEnergyDelta(), access
            );
            composition = step.composition();
            count = step.ingredientCount();
            maximumEnergy = Math.max(maximumEnergy, step.energyDelta());
            activated.addAll(step.reactions());
            if (step.outcome().ordinal() > worst.ordinal()) worst = step.outcome();
            if (worst == AlchemyMergeResolver.Outcome.CATASTROPHIC) break;
        }
        return new Result(MedicinalEffectResolver.resolve(composition, access), List.copyOf(activated), worst,
                maximumEnergy, count);
    }

    public record Result(MedicinalProfile profile, List<Identifier> reactions,
                         AlchemyMergeResolver.Outcome outcome, double maximumEnergyDelta, int ingredientCount) {
        public Result {
            reactions = reactions == null ? List.of() : List.copyOf(reactions);
        }
    }
}
