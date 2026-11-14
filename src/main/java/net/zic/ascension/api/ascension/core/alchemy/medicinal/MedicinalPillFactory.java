package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.PillGrade;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MedicinalPillFactory {
    private MedicinalPillFactory() {
    }

    public static ItemStack condense(MedicinalProfile profile, int ingredientCount, RegistryAccess access, long gameTime, double ingredientQuality) {
        return condense(profile, ingredientCount, access, gameTime, ingredientQuality, null, 0.0D);
    }

    public static ItemStack condense(MedicinalProfile profile, int ingredientCount, RegistryAccess access,
                                     long gameTime, double ingredientQuality, Identifier desiredEffect, double focusPrecision) {
        if (profile == null || profile.composition().isEmpty()) return ItemStack.EMPTY;
        List<MedicinalProfile.Effect> beneficial = new ArrayList<>();
        List<MedicinalProfile.Effect> harmful = new ArrayList<>();
        for (MedicinalProfile.Effect effect : profile.effects()) {
            if (effect.adverse()) harmful.add(effect);
            else beneficial.add(effect);
        }
        if (beneficial.isEmpty()) return ItemStack.EMPTY;

        Registry<MedicinalEffectDefinition> expressions = access == null ? null : MedicinalRegistries.EFFECTS.get(access);
        beneficial.sort(Comparator.<MedicinalProfile.Effect>comparingDouble(effect ->
                -weightedPotency(effect, expressions)).thenComparing(effect -> effect.id().toString()));
        harmful.sort(Comparator.comparingDouble(MedicinalProfile.Effect::potency).reversed()
                .thenComparing(effect -> effect.id().toString()));

        MedicinalProfile.Effect primary = beneficial.getFirst();
        if (desiredEffect != null) {
            for (MedicinalProfile.Effect candidate : beneficial) {
                if (candidate.id().equals(desiredEffect) && candidate.potency() >= primary.potency()
                        * Math.max(0.55D, 0.85D - Math.max(0.0D, Math.min(0.3D, focusPrecision)))) {
                    primary = candidate;
                    break;
                }
            }
        }
        final MedicinalProfile.Effect chosen = primary;
        double support = beneficial.stream().filter(effect -> effect != chosen)
                .mapToDouble(MedicinalProfile.Effect::potency).sum();
        double bonus = Math.min(primary.potency() * 0.35D, support * 0.08D);
        double steeringCost = primary == beneficial.getFirst() ? 1.0D : 0.92D;
        MedicinalProfile.Effect focused = new MedicinalProfile.Effect(primary.id(),
                Math.min(4096.0D, (primary.potency() + bonus) * steeringCost), primary.durationTicks(), false);
        List<MedicinalProfile.Effect> finalEffects = new ArrayList<>();
        finalEffects.add(focused);
        if (!harmful.isEmpty() && (profile.composition().toxicity() > 0.01D
                || profile.composition().substance().instability() > 0.5D)) {
            finalEffects.add(harmful.getFirst());
        }
        MedicinalProfile result = new MedicinalProfile(profile.composition(), finalEffects);
        var substance = profile.composition().substance();
        double refinementQuality = substance.purity() / (1.0D + substance.instability() * 0.15D);
        double quality = Math.max(0.0D, Math.min(1.0D,
                ingredientQuality * 0.90D + refinementQuality * 0.10D));
        int count = Math.min(4, Math.max(1, 1 + (Math.max(1, ingredientCount) - 2) / 3));
        ItemStack stack = new ItemStack(ModItems.PROCEDURAL_PILL.get(), count);
        stack.set(AscensionComponents.MEDICINAL_PILL.get(), result);
        MedicinalDecay.initialize(stack, gameTime);
        stack.set(AscensionComponents.PILL_DATA.get(), new AscensionComponents.PillData(
                AscensionComponents.PillData.rankForTier(substance.rankTier()), PillGrade.fromQuality(quality)
        ));
        stack.set(DataComponents.ITEM_NAME, nameFor(result, primary.id(), access));
        return stack;
    }

    private static double weightedPotency(MedicinalProfile.Effect effect, Registry<MedicinalEffectDefinition> expressions) {
        if (expressions == null) return effect.potency();
        MedicinalEffectDefinition definition = expressions.getValue(effect.id());
        int priority = definition == null ? 0 : definition.priority();
        return effect.potency() + Math.max(0, priority) * 1.25D;
    }

    private static Component nameFor(MedicinalProfile profile, Identifier effect, RegistryAccess access) {
        if (access != null) {
            Registry<MedicinalEffectDefinition> effects = MedicinalRegistries.EFFECTS.get(access);
            MedicinalEffectDefinition definition = effects.getValue(effect);
            if (definition != null && !definition.names().isEmpty()) {
                int index = Math.floorMod(profile.composition().hashCode(), definition.names().size());
                return Component.translatable(definition.names().get(index));
            }
        }
        return Component.translatable("ascension.alchemy.effect." + effect.getPath().replace('/', '.'))
                .append(Component.literal(" Pill"));
    }
}
