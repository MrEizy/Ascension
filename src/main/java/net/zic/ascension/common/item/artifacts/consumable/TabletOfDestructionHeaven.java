package net.zic.ascension.common.item.artifacts.consumable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;

import java.util.List;
import java.util.function.Consumer;

/**
 * Heaven Tablet — highest tier.
 * Tunnel: 4 wide x 7 tall x 22 deep | Cooldown: 5s
 * Supports normal drops and container linking.
 * Outline colour: cyan dye. Linked container: pulsing gold.
 */
public class TabletOfDestructionHeaven extends BaseTabletOfDestruction {

    private static final int COOLDOWN = 100;
    private static final int WIDTH = 4, HEIGHT = 7, DEPTH = 22;

    /** Vanilla cyan dye ARGB — used by the client outline renderer. */
    public static final int OUTLINE_COLOR = 0xFF_169C9C;

    /** Gold colour for the linked-container pulse outline. */
    public static final int LINKED_COLOR  = 0xFF_FFC700;

    public TabletOfDestructionHeaven(Properties properties) {
        super(properties);
    }

    @Override protected int getCooldownTicks()             { return COOLDOWN; }
    @Override protected int getWidth()                     { return WIDTH;    }
    @Override protected int getHeight()                    { return HEIGHT;   }
    @Override protected int getDepth()                     { return DEPTH;    }

    @Override
    public boolean supportsDropBlocks() {
        return true;
    }

    @Override
    public boolean supportsContainerLinking() {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        tooltip.accept(Component.translatable(
                        "item.ascension.tablet_of_destruction_heaven.link_info")
                .withStyle(ChatFormatting.DARK_GRAY));

        LinkedContainerData link = getLinkedContainer(stack);
        if (link.pos() != null
                && context.level() != null
                && link.dimension() != null
                && link.dimension().equals(
                context.level().dimension().identifier().toString())) {
            BlockPos p = link.pos();
            String blockName = context.level()
                    .getBlockState(p).getBlock().getName().getString();
            tooltip.accept(Component.translatable(
                            "item.ascension.tablet_of_destruction_heaven.linked_to",
                            blockName, p.getX(), p.getY(), p.getZ())
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable(
                "item.ascension.tablet_of_destruction_heaven.cooldown");
    }
}
