package net.zic.ascension.impl.core.qi;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.qi.ItemQiHandler;
import net.zic.ascension.api.ascension.core.qi.QiHandler;
import net.zic.ascension.common.item.components.AscensionComponents;

/**
 * TODO update max capacity to use a tier string that links to a config?
 */
public abstract class SimpleItemQiHandler implements ItemQiHandler {

    private final ItemStack itemStack;
    private final long CAPACITY;
    public SimpleItemQiHandler(ItemStack itemStack, long capacity){
        this.itemStack = itemStack;
        CAPACITY = capacity;

    }


    @Override
    public boolean tryConsume(int amount) {
        long newQi = Math.min( getQi() -amount,CAPACITY);
        if(newQi < 0) return false;
        setQi(newQi);
        return true;
    }

    @Override
    public int insertQi(int amount) {
        long qi = getQi();
        int change = Math.toIntExact(Math.min(amount, CAPACITY - qi));
        setQi(qi+change);
        return change;
    }

    @Override
    public int extractQi(int amount) {
        long qi = getQi();
        int change = Math.toIntExact(Math.min(qi, amount));
        setQi(qi-change);
        return change;
    }


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

    public ItemStack getItem(){
        return itemStack;
    }
}
