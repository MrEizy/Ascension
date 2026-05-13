package net.thejadeproject.ascension.common.items.stones;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;


import java.util.List;

public class SpatialStoneItem extends Item {
    private final int rowsAdded;
    private final String tierName;

    public SpatialStoneItem(int rowsAdded, String tierName, Rarity rarity) {
        super(new Properties()
                .stacksTo(1)
                .rarity(rarity)
                .component(ModDataComponents.EXTRA_SLOTS,rowsAdded*9));
        this.rowsAdded = rowsAdded;
        this.tierName = tierName;
    }
    public int getRowsAdded() {
        return rowsAdded;
    }

    public static boolean isSpatialStone(ItemStack stack) {
        return stack.getItem() instanceof SpatialStoneItem;
    }
}
