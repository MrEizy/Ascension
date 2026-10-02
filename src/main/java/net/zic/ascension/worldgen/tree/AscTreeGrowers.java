package net.zic.ascension.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.AscConfiguredFeatures;

import java.util.Optional;

public class AscTreeGrowers {
    public static final TreeGrower PEACH = new TreeGrower(AscensionCraft.MOD_ID + ":peach",
            Optional.empty(), Optional.of(AscConfiguredFeatures.PEACH_TREE_KEY), Optional.empty());
}
