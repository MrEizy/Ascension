package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalComposition;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalDecay;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalEffectResolver;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalIngredientQuality;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalPillFactory;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalProfile;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalReactionResolver;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AlchemyPrediction {
    private AlchemyPrediction() {
    }

    public static List<ItemStack> predict(Container items, AlchemyContext context,
                                          AlchemyRefinementPlan plan, RegistryAccess access, Level level) {
        List<AlchemySubstance> extracts = new ArrayList<>();
        List<ItemStack> ingredients = new ArrayList<>();
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = items.getItem(slot);
            if (stack.isEmpty()) continue;
            AlchemyMaterial material = AlchemyMaterials.resolve(stack, context).orElse(null);
            if (material == null) return List.of();
            AlchemySubstance substance = AlchemyPlanEvaluator.extract(material, context, plan, access);
            if (substance.isEmpty()) return List.of();
            extracts.add(substance);
            ingredients.add(stack);
        }
        if (extracts.isEmpty()) return List.of();

        MedicinalComposition composition = MedicinalComposition.EMPTY;
        int ingredientCount = 0;
        AlchemyProcessHooks.ProcessSettings settings = AlchemyPlanEvaluator.reactions(plan, access);
        for (AlchemySubstance extract : extracts) {
            MedicinalReactionResolver.Step step = MedicinalReactionResolver.add(composition, ingredientCount, extract,
                    settings.safeEnergyDelta(), settings.explosionEnergyDelta(), access);
            if (step.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC)
                return List.of(new ItemStack(ModItems.PILL_RESIDUE.get()));
            composition = step.composition();
            ingredientCount = step.ingredientCount();
        }
        composition = AlchemyPlanEvaluator.stabilize(composition, ingredientCount, plan, access);
        MedicinalProfile profile = MedicinalEffectResolver.resolve(composition, access);
        double quality = AlchemyPlanEvaluator.finalQuality(MedicinalIngredientQuality.average(ingredients), plan, access);
        double precision = AlchemyPlanEvaluator.technique(plan, AlchemyTechnique.Stage.CONDENSATION,
                access).focusPrecision();
        long gameTime = MedicinalDecay.now(level);

        ItemStack primary = MedicinalPillFactory.condense(profile, ingredientCount, access, gameTime,
                quality, plan.focus(), precision);
        if (primary.isEmpty()) return List.of(new ItemStack(ModItems.PILL_RESIDUE.get()));
        List<ItemStack> predictions = new ArrayList<>();
        predictions.add(primary);
        Set<Identifier> selectedEffects = new HashSet<>();
        selectedEffects.add(primary.get(AscensionComponents.MEDICINAL_PILL.get()).effects().getFirst().id());

        List<MedicinalProfile.Effect> alternatives = MedicinalPillFactory.rankedBeneficialEffects(profile, access);
        for (MedicinalProfile.Effect effect : alternatives) {
            if (selectedEffects.contains(effect.id())) continue;
            ItemStack candidate = MedicinalPillFactory.condense(profile, ingredientCount, access,
                    gameTime, quality, effect.id(), precision);
            if (candidate.isEmpty()) continue;
            MedicinalProfile candidateProfile = candidate.get(AscensionComponents.MEDICINAL_PILL.get());
            if (candidateProfile == null || candidateProfile.effects().isEmpty()) continue;
            Identifier selected = candidateProfile.effects().getFirst().id();
            if (selectedEffects.add(selected)) {
                predictions.add(candidate);
                break;
            }
        }
        return List.copyOf(predictions);
    }
}
