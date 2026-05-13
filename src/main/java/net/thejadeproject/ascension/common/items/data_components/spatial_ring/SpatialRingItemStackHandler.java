package net.thejadeproject.ascension.common.items.data_components.spatial_ring;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

public class SpatialRingItemStackHandler extends ItemStackHandler {
    public enum Type{
        INVENTORY,
        UPGRADES,
        MODIFIERS
    }
    public Type type;
    public ItemStack stack;
    public SpatialRingItemStackHandler(ItemStack stack, Type type, NonNullList<ItemStack> items){
        super(items);
        this.type = type;
        this.stack = stack;
    }
    @Override
    protected void onContentsChanged(int slot) {
        // save back into component
        super.onContentsChanged(slot);
        //System.out.println("modifying slot : "+slot);
        //System.out.println("has item : "+getStackInSlot(slot));
        if(stack.has(ModDataComponents.SPIRIT_RING_DATA)){
            SpatialRingComponent component= stack.get(ModDataComponents.SPIRIT_RING_DATA);
            if(type == Type.INVENTORY){
                component.addItem(getStackInSlot(slot),slot);
            }else if(type == Type.UPGRADES){
                component.addUpgradeItem(getStackInSlot(slot),slot);
            }else if(type == Type.MODIFIERS){
                component.addModifierItem(getStackInSlot(slot),slot);
            }
            stack.set(ModDataComponents.SPIRIT_RING_DATA, component.copy());//syncs
        }
    }

}
