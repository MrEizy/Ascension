package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTabletOfDestruction;

import java.util.List;

public class TabletOfDestructionHeaven extends BaseTabletOfDestruction {
    private static final int COOLDOWN = 100; // 5 seconds
    private static final int WIDTH = 4, HEIGHT = 7, DEPTH = 22;

    public TabletOfDestructionHeaven(Properties properties) {
        super(properties);
    }

    @Override protected int getCooldownTicks()             { return COOLDOWN; }
    @Override protected int getWidth()                     { return WIDTH; }
    @Override protected int getHeight()                    { return HEIGHT; }
    @Override protected int getDepth()                     { return DEPTH; }
    @Override protected boolean supportsDropBlocks()       { return true; }
    @Override protected boolean supportsContainerLinking() { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Drop-blocks toggle status
        boolean drop = isDropBlocksEnabled(stack);
        Component status = Component.literal(String.valueOf(drop))
                .withStyle(drop ? ChatFormatting.GREEN : ChatFormatting.RED);
        tooltipComponents.add(
                Component.translatable("item.ascension.tablet_of_destruction_heaven.drop_blocks")
                        .append(status));
        tooltipComponents.add(
                Component.translatable("ascension.tablet.toggle_mode_info"));

        // Container linking hint
        tooltipComponents.add(
                Component.translatable("item.ascension.tablet_of_destruction_heaven.link_info"));

        // Linked container coordinates (only shown when in the correct dimension)
        LinkedContainerData link = getLinkedContainer(stack);
        if (link.pos() != null
                && context.level() != null
                && link.dimension() != null
                && link.dimension().equals(
                context.level().dimension().location().toString())) {
            BlockPos p = link.pos();
            String blockName = context.level().getBlockState(p).getBlock().getName().getString();
            tooltipComponents.add(
                    Component.translatable(
                            "item.ascension.tablet_of_destruction_heaven.linked_to",
                            blockName, p.getX(), p.getY(), p.getZ()));
        }
    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable("item.ascension.tablet_of_destruction_heaven.cooldown");
    }
}