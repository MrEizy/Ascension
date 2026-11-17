package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.common.item.herbs.HerbItem;

import java.util.List;

public final class MedicinalIngredientQuality {
    private MedicinalIngredientQuality() {
    }

    public static double average(List<ItemStack> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) return 0.35D;
        double total = 0.0D;
        int count = 0;
        for (ItemStack stack : ingredients) {
            if (stack == null || stack.isEmpty()) continue;
            total += forStack(stack);
            count++;
        }
        return count == 0 ? 0.35D : Mth.clamp(total / count, 0.0D, 1.0D);
    }

    public static double forStack(ItemStack stack) {
        if (stack.getItem() instanceof HerbItem herb) {
            var data = herb.data(stack);
            double base = switch (Mth.clamp(data.qualityTier(), 0, 4)) {
                case 0 -> 0.18D;
                case 1 -> 0.37D;
                case 2 -> 0.58D;
                case 3 -> 0.77D;
                default -> 0.94D;
            };
            return Mth.clamp(base + Math.min(0.05D, data.ageTier() * 0.01D), 0.0D, 1.0D);
        }
        return 0.37D;
    }
}
