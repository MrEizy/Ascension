package net.zic.ascension.common.item.qi_holder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.function.Consumer;


public class SpiritStone extends Item {
    public SpiritStone(Properties properties) {
        super(properties.component(AscensionComponents.ITEM_TIER,"Low"));
    }

    //TODO see TODO on Component for plans with this
    @Override
    public Component getName(ItemStack itemStack) {
        return Component.literal(itemStack.get(AscensionComponents.ITEM_TIER)+" Tier ").append(super.getName(itemStack));
    }



}
