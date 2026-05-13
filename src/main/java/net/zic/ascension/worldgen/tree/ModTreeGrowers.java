package net.zic.ascension.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.ModConfiguredFeatures;

import java.util.Optional;

public class ModTreeGrowers {
    public static final TreeGrower GOLDEN_PALM = new TreeGrower(AscensionCraft.MOD_ID + ":golden_palm",
            Optional.empty(), Optional.of(ModConfiguredFeatures.GOLDEN_PALM_KEY), Optional.empty());
    public static final TreeGrower IRONWOOD = new TreeGrower(AscensionCraft.MOD_ID + ":ironwood",
            Optional.empty(), Optional.of(ModConfiguredFeatures.IRONWOOD_KEY), Optional.empty());
}
