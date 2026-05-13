package net.thejadeproject.ascension.common.items.tools.herbs;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MortarAndPestle extends Item {

    public MortarAndPestle(Properties properties){
        super (new Item.Properties()
                .durability(128)
                .setNoRepair()
                .stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (!itemStack.isDamageableItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack damaged = itemStack.copy();
        int newDamage = damaged.getDamageValue() + 1;

        if (newDamage >= damaged.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        damaged.setDamageValue(newDamage);
        return damaged;
    }


}
