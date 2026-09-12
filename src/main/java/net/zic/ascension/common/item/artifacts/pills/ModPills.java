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

import java.util.Optional;

public final class ModPills {

    public static final PillItem.Definition FASTING = PillItem.Definition.builder((level, player, stack, data) -> {
                FoodData food = player.getFoodData();
                if (food.getFoodLevel() >= 20 && food.getSaturationLevel() >= 20.0F) {
                    return false;
                }

                food.setFoodLevel(20);
                food.setSaturation(20.0F);
                return true;
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
                if (provider == null || provider.getMaxQi() <= 0.0D || provider.getQi() >= provider.getMaxQi()) {
                    return false;
                }

                provider.regenQi(provider.getMaxQi() * qiRestoreFraction(data));
                return true;
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
                    Math.round(qiRestoreFraction(data) * 100.0D)
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

    private static double qiRestoreFraction(AscensionComponents.PillData data) {
        AscensionComponents.PillData resolved = data == null ? AscensionComponents.PillData.DEFAULT : data;
        double purityMultiplier = 0.5D + resolved.purity() / 100.0D;
        return Math.min(1.0D, 0.08D * resolved.potency() * purityMultiplier);
    }
}
