package net.zic.ascension.client.innerworld;

import net.minecraft.core.BlockPos;

import java.util.List;

/** Client copy of the vein positions the last Divine Sense revealed. The Divine Sense wave decides WHEN each one lights up. */
public final class InnerWorldVeinClientState {
    private InnerWorldVeinClientState() {
    }

    private static List<BlockPos> veins = List.of();

    public static void set(List<BlockPos> positions) {
        veins = List.copyOf(positions);
    }

    public static List<BlockPos> get() {
        return veins;
    }

    public static void clear() {
        veins = List.of();
    }
}