package net.zic.ascension.common.item.artifacts.consumable;

import net.minecraft.network.chat.Component;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;

public class TabletOfDestructionHuman extends BaseTabletOfDestruction {

    private static final int COOLDOWN = 400;
    private static final int WIDTH = 2, HEIGHT = 3, DEPTH = 15;

    /** Vanilla gray dye ARGB — used by the client outline renderer. */
    public static final int OUTLINE_COLOR = 0xFF_9D9D97;

    public TabletOfDestructionHuman(Properties properties) {
        super(properties);
    }

    @Override protected int getCooldownTicks()             { return COOLDOWN; }
    @Override protected int getWidth()                     { return WIDTH;    }
    @Override protected int getHeight()                    { return HEIGHT;   }
    @Override protected int getDepth()                     { return DEPTH;    }
    @Override protected boolean supportsDropBlocks()       { return false;    }
    @Override protected boolean supportsContainerLinking() { return false;    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable(
                "item.ascension.tablet_of_destruction_human.cooldown");
    }
}
