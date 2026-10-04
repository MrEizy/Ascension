package net.zic.ascension.datagen;

import net.minecraft.data.BlockFamily;
import net.zic.ascension.common.blocks.ModBlocks;

/** Plank-based block sets, shared by the model and recipe providers. */
public class AscBlockFamilies {
    public static final BlockFamily IRONWOOD_PLANKS = new BlockFamily.Builder(ModBlocks.IRONWOOD_PLANKS.get())
            .button(ModBlocks.IRONWOOD_BUTTON.get())
            .fence(ModBlocks.IRONWOOD_FENCE.get())
            .fenceGate(ModBlocks.IRONWOOD_FENCE_GATE.get())
            .pressurePlate(ModBlocks.IRONWOOD_PRESSURE_PLATE.get())
            .slab(ModBlocks.IRONWOOD_SLAB.get())
            .stairs(ModBlocks.IRONWOOD_STAIRS.get())
            .door(ModBlocks.IRONWOOD_DOOR.get())
            .trapdoor(ModBlocks.IRONWOOD_TRAPDOOR.get())
            .recipeGroupPrefix("wooden")
            .recipeUnlockedBy("has_planks")
            .getFamily();
}
