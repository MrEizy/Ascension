package net.zic.ascension.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public class PillCauldronInput implements RecipeInput {
    public int inputSlots;
    public List<ItemStack> items;
    public int currentHeat; // Add this field

    public PillCauldronInput(int inputSlots, List<ItemStack> item, int currentHeat) {
        this.items = item;
        this.inputSlots = inputSlots;
        this.currentHeat = currentHeat; // Initialize it
    }

    public int getCurrentHeat() {
        return currentHeat; // Return the field, not call a method
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        if(i >= items.size()) return null;
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size(); // Should return actual size, not 0
    }
}