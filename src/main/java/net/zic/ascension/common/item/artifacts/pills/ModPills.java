package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.stamina.StaminaService;

/**
 * Define effects that pills can have in this class
  */
public final class ModPills {

    public static final PillItem.Definition FASTING = PillItem.Definition.builder((level, player, stack, data) -> {
                FoodData food = player.getFoodData();
                food.setFoodLevel(20);
                food.setSaturation(20.0F);
            })
            .canConsume((level, player, stack, data) -> {
                FoodData food = player.getFoodData();
                return food.getFoodLevel() < 20 || food.getSaturationLevel() < 20.0F;
            })
            .effectDescription(data -> Component.translatable("ascension.pill.fasting_pill.effect"))
            .build();

    public static final PillItem.Definition QI_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                if (provider != null && provider.getMaxQi() > 0.0D) {
                    provider.regenQi(provider.getMaxQi() * restoreFraction(data, 0.30D));
                }
            })
            .canConsume((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                return provider != null && provider.getMaxQi() > 0.0D && provider.getQi() < provider.getMaxQi();
            })
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.qi_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.30D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition REGENERATION = PillItem.Definition.builder((level, player, stack, data) ->
                    player.heal((float) (player.getMaxHealth() * restoreFraction(data, 0.20D))))
            .canConsume((level, player, stack, data) -> player.getHealth() < player.getMaxHealth())
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.regeneration_pill.effect",
                    Math.round(restoreFraction(data, 0.20D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition STAMINA_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                if (maximum > 0.0D) {
                    StaminaService.restore(
                            player,
                            AscensionResourceSources.DIRECT,
                            maximum * restoreFraction(data, 0.40D)
                    );
                }
            })
            .canConsume((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                return maximum > 0.0D && StaminaService.getStamina(player) < maximum;
            })
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.stamina_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.40D) * 100.0D)
            ))
            .build();

    private ModPills() {
    }


    public static ItemStack maximumStack(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.MAXIMUM);
        return stack;
    }

    private static double restoreFraction(AscensionComponents.PillData data, double baseFraction) {
        AscensionComponents.PillData resolved = data == null ? AscensionComponents.PillData.DEFAULT : data;
        return Math.min(1.0D, baseFraction * resolved.strengthMultiplier());
    }
}
