package net.zic.ascension.common.item.artifacts.consumable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;

import java.util.List;
import java.util.function.Consumer;

/**
 * Earth Tablet — mid tier.
 * Tunnel: 3 wide x 5 tall x 18 deep | Cooldown: 10s
 * Supports normal block drops. No container linking.
 * Outline colour: green dye.
 */
public class TabletOfDestructionEarth extends BaseTabletOfDestruction {

    private static final int COOLDOWN = 200;
    private static final int WIDTH = 3, HEIGHT = 5, DEPTH = 18;

    /** Vanilla green dye ARGB — used by the client outline renderer. */
    public static final int OUTLINE_COLOR = 0xFF_5E7C16;

    public TabletOfDestructionEarth(Properties properties) {
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
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(
                        "item.ascension.tablet_of_destruction_earth.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable(
                "item.ascension.tablet_of_destruction_earth.cooldown");
    }
}
