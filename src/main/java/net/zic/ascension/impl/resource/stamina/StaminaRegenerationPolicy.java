package net.zic.ascension.impl.resource.stamina;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

public final class StaminaRegenerationPolicy {
    public static final double SATURATED_MULTIPLIER = 1.10D;
    public static final double WELL_FED_MULTIPLIER = 1.00D;
    public static final double HUNGRY_MULTIPLIER = 0.70D;
    public static final double STARVING_MULTIPLIER = 0.35D;

    private StaminaRegenerationPolicy() {
    }

    public static double getRegenerationRate(ServerPlayer player) {
        if (player == null) {
            return 0.0D;
        }

        return StaminaService.getRegenerationRate(player) * getHungerMultiplier(player);
    }

    public static double getHungerMultiplier(ServerPlayer player) {
        if (player == null) {
            return 0.0D;
        }

        FoodData foodData = player.getFoodData();

        if (foodData.getSaturationLevel() > 0.0F) {
            return SATURATED_MULTIPLIER;
        }

        int foodLevel = foodData.getFoodLevel();

        if (foodLevel >= 12) {
            return WELL_FED_MULTIPLIER;
        }

        if (foodLevel >= 6) {
            return HUNGRY_MULTIPLIER;
        }

        if (foodLevel > 0) {
            return STARVING_MULTIPLIER;
        }

        return 0.0D;
    }
}