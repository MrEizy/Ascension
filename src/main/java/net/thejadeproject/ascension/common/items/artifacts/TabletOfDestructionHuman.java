package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTabletOfDestruction;

import java.util.List;

public class TabletOfDestructionHuman extends BaseTabletOfDestruction {
    private static final int COOLDOWN = 400; // 20 seconds
    private static final int WIDTH = 2, HEIGHT = 3, DEPTH = 15;

    public TabletOfDestructionHuman(Properties properties) {
        super(properties);
    }

    @Override protected int getCooldownTicks() { return COOLDOWN; }
    @Override protected int getWidth() { return WIDTH; }
    @Override protected int getHeight() { return HEIGHT; }
    @Override protected int getDepth() { return DEPTH; }
    @Override protected boolean supportsDropBlocks() { return false; }
    @Override protected boolean supportsContainerLinking() { return false; }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable("ascension.tablet.human.cooldown");
    }
}