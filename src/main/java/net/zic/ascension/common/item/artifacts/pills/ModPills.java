package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyAffinities;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyProperties;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.stamina.StaminaService;

import java.util.Optional;

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
            .property(AlchemyProperties.CLEANSING, 7.0D)
            .property(AlchemyProperties.SPIRIT_NOURISHMENT, 5.0D)
            .property(AlchemyProperties.RESTORATION, 7.0D)
            .property(AlchemyProperties.VITALITY, 4.0D)
            .affinity(AlchemyAffinities.LIFE, 6.0D)
            .affinity(AlchemyAffinities.WOOD, 1.0D)
            .effectDescription(data -> Component.translatable("ascension.pill.fasting_pill.effect"))
            .build();

    public static final PillItem.Definition QI_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                if (provider != null && provider.getMaxQi() > 0.0D) {
                    provider.regenQi(provider.getMaxQi() * restoreFraction(data, 0.08D));
                }
            })
            .canConsume((level, player, stack, data) -> {
                EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                return provider != null && provider.getMaxQi() > 0.0D && provider.getQi() < provider.getMaxQi();
            })
            .property(AlchemyProperties.REINFORCEMENT, 4.0D)
            .property(AlchemyProperties.ESSENCE_GATHERING, 8.0D)
            .property(AlchemyProperties.HEATING, 5.0D)
            .property(AlchemyProperties.MARROW_CLEANSING, 3.0D)
            .property(AlchemyProperties.CLEANSING, 2.0D)
            .affinity(AlchemyAffinities.FIRE, 10.0D)
            .affinity(AlchemyAffinities.YANG, 3.0D)
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.qi_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.08D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition REGENERATION = PillItem.Definition.builder((level, player, stack, data) ->
                    player.heal((float) (player.getMaxHealth() * restoreFraction(data, 0.05D))))
            .canConsume((level, player, stack, data) -> player.getHealth() < player.getMaxHealth())
            .property(AlchemyProperties.REINFORCEMENT, 9.0D)
            .property(AlchemyProperties.MARROW_CLEANSING, 4.0D)
            .property(AlchemyProperties.VITALITY, 11.0D)
            .property(AlchemyProperties.RESTORATION, 5.0D)
            .property(AlchemyProperties.SPIRIT_NOURISHMENT, 2.0D)
            .property(AlchemyProperties.BLOOD_NOURISHMENT, 6.0D)
            .affinity(AlchemyAffinities.WOOD, 4.0D)
            .affinity(AlchemyAffinities.LIFE, 5.0D)
            .affinity(AlchemyAffinities.BLOOD, 5.0D)
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.regeneration_pill.effect",
                    Math.round(restoreFraction(data, 0.05D) * 100.0D)
            ))
            .build();

    public static final PillItem.Definition STAMINA_REPLENISHING = PillItem.Definition.builder((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                if (maximum > 0.0D) {
                    StaminaService.restore(
                            player,
                            AscensionResourceSources.DIRECT,
                            maximum * restoreFraction(data, 0.08D)
                    );
                }
            })
            .canConsume((level, player, stack, data) -> {
                double maximum = StaminaService.getMaximumStamina(player);
                return maximum > 0.0D && StaminaService.getStamina(player) < maximum;
            })
            .property(AlchemyProperties.CIRCULATION, 5.0D)
            .property(AlchemyProperties.REINFORCEMENT, 9.0D)
            .property(AlchemyProperties.VITALITY, 9.0D)
            .property(AlchemyProperties.MARROW_CLEANSING, 4.0D)
            .property(AlchemyProperties.RESTORATION, 5.0D)
            .property(AlchemyProperties.SPIRIT_NOURISHMENT, 2.0D)
            .affinity(AlchemyAffinities.LIGHTNING, 6.0D)
            .affinity(AlchemyAffinities.YANG, 1.5D)
            .affinity(AlchemyAffinities.WOOD, 4.0D)
            .affinity(AlchemyAffinities.LIFE, 4.0D)
            .effectDescription(data -> Component.translatable(
                    "ascension.pill.stamina_replenishing_pill.effect",
                    Math.round(restoreFraction(data, 0.08D) * 100.0D)
            ))
            .build();

    private ModPills() {
    }

    public static Optional<ItemStack> condense(AlchemyBatch batch) {
        if (batch == null || batch.isEmpty()) {
            return Optional.empty();
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof PillItem pill) || !pill.definition().matches(batch.substance())) {
                continue;
            }

            ItemStack stack = new ItemStack(pill);
            stack.set(AscensionComponents.PILL_DATA.get(), createPillData(batch));
            return Optional.of(stack);
        }

        return Optional.empty();
    }

    public static AscensionComponents.PillData createPillData(AlchemyBatch batch) {
        double effectivePurity = Mth.clamp(
                batch.substance().purity() / (1.0D + batch.substance().instability()),
                0.0D,
                1.0D
        );
        int purity = Mth.clamp((int) Math.round(effectivePurity * 100.0D), 0, 100);
        int majorRealm = batch.substance().majorRealm();
        int minorRealm = batch.substance().minorRealm();

        return new AscensionComponents.PillData(
                AscensionComponents.PillData.rankForRealm(majorRealm),
                majorRealm,
                minorRealm,
                purity,
                batch.substance().potency()
        );
    }

    public static ItemStack maximumStack(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.MAXIMUM);
        return stack;
    }

    private static double restoreFraction(AscensionComponents.PillData data, double potencyScale) {
        AscensionComponents.PillData resolved = data == null ? AscensionComponents.PillData.DEFAULT : data;
        double purityMultiplier = 0.5D + resolved.purity() / 100.0D;
        return Math.min(1.0D, potencyScale * resolved.potency() * purityMultiplier);
    }
}
