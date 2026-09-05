package net.zic.ascension.impl.core.qi;

import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.qi.QiHandler;

/**
 * TODO update max capacity to use a tier string that links to a config?
 */
public class SimpleItemQiHandler implements QiHandler {

    private ItemStack itemStack;
    private final long CAPACITY;
    public SimpleItemQiHandler(ItemStack itemStack, long capacity){
        this.itemStack = itemStack;
        CAPACITY = capacity;
    }

    @Override
    public boolean tryConsume(int amount) {
        return false;
    }

    @Override
    public int insertQi(int amount) {
        return 0;
    }

    @Override
    public int extractQi(int amount) {
        return 0;
    }

    @Override
    public long getQi() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }
}
