package net.zic.ascension.common.gui.menus.jade_bottle;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.common.util.ModTags;

public class PillSlot extends Slot {
    public static final int MAX_STACK = 8;

    public PillSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.PILLS);
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK;
    }


    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return MAX_STACK;
    }
}
