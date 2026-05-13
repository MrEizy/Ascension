package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTabletOfDestruction;

import java.util.List;

public class TabletOfDestructionEarth extends BaseTabletOfDestruction {
    private static final int COOLDOWN = 200; // 10 seconds
    private static final int WIDTH = 3, HEIGHT = 5, DEPTH = 18;

    public TabletOfDestructionEarth(Properties properties) {
        super(properties);
    }

    @Override protected int getCooldownTicks() { return COOLDOWN; }
    @Override protected int getWidth() { return WIDTH; }
    @Override protected int getHeight() { return HEIGHT; }
    @Override protected int getDepth() { return DEPTH; }
    @Override protected boolean supportsDropBlocks() { return true; }
    @Override protected boolean supportsContainerLinking() { return false; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        boolean drop = isDropBlocksEnabled(stack);
        Component status = Component.literal(String.valueOf(drop))
                .withStyle(drop ? ChatFormatting.GREEN : ChatFormatting.RED);
        tooltipComponents.add(
                Component.translatable("ascension.tablet.drop_blocks").append(status));
        tooltipComponents.add(
                Component.translatable("ascension.tablet.toggle_mode_info"));
    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable("ascension.tablet.cooldown");
    }
}