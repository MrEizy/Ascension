package net.thejadeproject.ascension.common.items.herbs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Base class for simple (non-plantable, non-block) herbs.
 *
 * Use instead of plain {@code Item} for any herb that participates in the
 * cauldron quality system. Adds Quality and Age tooltip lines when those
 * data components are present on the stack.
 *
 * Registration example:
 *   public static final DeferredItem<Item> GOLDEN_SUN_LEAF = ITEMS.register("golden_sun_leaf",
 *       () -> new HerbItem(new Item.Properties().food(ModFoodProperties.GOLDEN_SUN_LEAF)));
 */
public class HerbItem extends Item {

    public HerbItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        HerbQuality.appendHerbTooltip(stack, tooltip);
    }
}