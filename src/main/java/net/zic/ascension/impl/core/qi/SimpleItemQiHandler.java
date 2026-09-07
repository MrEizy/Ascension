package net.zic.ascension.impl.core.qi;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.qi.QiHandler;
import net.zic.ascension.common.item.components.AscensionComponents;

/**
 * TODO update max capacity to use a tier string that links to a config?
 */
public class SimpleItemQiHandler implements QiHandler {

    private final ItemStack itemStack;
    private final long CAPACITY;
    public SimpleItemQiHandler(ItemStack itemStack, long capacity){
        this.itemStack = itemStack;
        CAPACITY = capacity;
    }

    @Override
    public boolean tryConsume(int amount) {
        return false;
    }//TODO

    @Override
    public int insertQi(int amount) {
        return 0;
    }//TODO

    @Override
    public int extractQi(int amount) {
        return 0;
    }//TODO

    @Override
    public long getQi() {
        return itemStack.getOrDefault(AscensionComponents.ITEM_QI,0L);
    }

    public void setQi(long amount){
        itemStack.set(AscensionComponents.ITEM_QI,Math.min(amount,CAPACITY));
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    @Override
    public boolean isFull() {
        return getQi() == getCapacity();
    }
}
