package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.world.food.FoodData;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;

public final class ModPills {

    public static final PillItem.Definition FASTING = new PillItem.Definition((level, player, stack) -> {
        FoodData food = player.getFoodData();
        if (food.getFoodLevel() >= 20 && food.getSaturationLevel() >= 20.0F) {
            return false;
        }

        food.setFoodLevel(20);
        food.setSaturation(20.0F);
        return true;
    });

    public static final PillItem.Definition QI_REPLENISHING = new PillItem.Definition((level, player, stack) -> {
        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (provider == null || provider.getMaxQi() <= 0.0D || provider.getQi() >= provider.getMaxQi()) {
            return false;
        }

        provider.regenQi(provider.getMaxQi() * 0.25D);
        return true;
    });






    private ModPills() {
    }
}
