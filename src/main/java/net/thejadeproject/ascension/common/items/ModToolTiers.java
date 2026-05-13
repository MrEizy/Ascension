package net.thejadeproject.ascension.common.items;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.thejadeproject.ascension.util.ModTags;

public class ModToolTiers {
    public static final Tier JADE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_JADE_TOOL,
            1900, 10, 6f, 20, () -> Ingredient.of(ModItems.JADE));

    public static final Tier SPIRITUAL_STONE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_SPIRITUAL_STONE_TOOL,
            2048, 9, 6.5f, 22, () -> Ingredient.EMPTY);
}
