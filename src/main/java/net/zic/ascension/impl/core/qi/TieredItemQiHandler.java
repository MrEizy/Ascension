package net.zic.ascension.impl.core.qi;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.configuration.item.qi_capacity.TierCapacityHelper;

public class TieredItemQiHandler extends SimpleItemQiHandler{
    public TieredItemQiHandler(ItemStack itemStack) {
        super(itemStack, TierCapacityHelper.getCapacity(itemStack));
    }

    @Override
    public ItemStack tryConsumeAndSplit(int amount) {
        if(!isFull() | amount > getQi()) return ItemStack.EMPTY;

        ItemStack splitStack =  getItem().split(getItem().count()-1);
        tryConsume(amount);
        return splitStack;
    }

    @Override
    public ItemStack insertQiAndSplit(int amount) {
        if(isFull()) return ItemStack.EMPTY;

        ItemStack splitStack =  getItem().split(getItem().count()-1);
        insertQi(amount);

        return splitStack;
    }

    @Override
    public ItemStack extractQiAndSplit(int amount) {
        if(!isFull()) return ItemStack.EMPTY;

        ItemStack splitStack =  getItem().split(getItem().count()-1);
        extractQi(amount);

        return splitStack;
    }

    public String getTier(){
        return getItem().get(AscensionComponents.ITEM_TIER);
    }
}
