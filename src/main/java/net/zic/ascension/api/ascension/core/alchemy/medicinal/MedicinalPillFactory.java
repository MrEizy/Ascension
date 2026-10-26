package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.PillGrade;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

/**
 * Condenses any stable, effect-bearing medicinal composition into the single procedural pill item.
 * Each batch preserves its complete effect profile and original rank and grade on the stack.
 */
public final class MedicinalPillFactory {
    private MedicinalPillFactory() {
    }

    public static ItemStack condense(MedicinalProfile profile, int ingredientCount) {
        if (profile == null || profile.composition().isEmpty() || profile.effects().isEmpty()) return ItemStack.EMPTY;
        MedicinalComposition composition = profile.composition();
        double purity = composition.substance().purity();
        double instability = composition.substance().instability();
        double quality = Math.max(0.0D, Math.min(1.0D, purity / (1.0D + instability * 0.15D)));
        int count = Math.min(4, Math.max(1, 1 + (Math.max(1, ingredientCount) - 2) / 3));
        ItemStack stack = new ItemStack(ModItems.PROCEDURAL_PILL.get(), count);
        stack.set(AscensionComponents.MEDICINAL_PILL.get(), profile);
        stack.set(AscensionComponents.PILL_DATA.get(), new AscensionComponents.PillData(
                AscensionComponents.PillData.rankForTier(composition.substance().rankTier()),
                PillGrade.fromQuality(quality)
        ));
        return stack;
    }
}
