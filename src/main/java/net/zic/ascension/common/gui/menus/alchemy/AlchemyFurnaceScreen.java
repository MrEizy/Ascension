package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;

public class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {
    private static final Identifier TEXTURE = AscensionCraft.prefix("textures/gui/pill_cauldron_low_human/pill_cauldron_low_human_menu.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        titleLabelX = 8;
        titleLabelY = -11;
        inventoryLabelY = 73;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
                0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int ordinal = Math.max(0, Math.min(AlchemyFurnaceBlockEntity.Phase.values().length - 1, menu.phase()));
        AlchemyFurnaceBlockEntity.Phase phase = AlchemyFurnaceBlockEntity.Phase.values()[ordinal];
        int duration = AlchemyFurnaceBlockEntity.duration(phase);
        String label = switch (phase) {
            case IDLE -> "Pulse redstone";
            case EXTRACTION -> "Extract";
            case REACTION -> "React";
            case STABILIZATION -> "Stabilize";
            case CONDENSATION -> "Condense";
            case SUCCESS -> "Complete";
            case FAILURE -> "Failed";
        };
        String status = phase == AlchemyFurnaceBlockEntity.Phase.IDLE
                || phase == AlchemyFurnaceBlockEntity.Phase.SUCCESS
                || phase == AlchemyFurnaceBlockEntity.Phase.FAILURE
                ? label : label + " " + (100 * Math.min(menu.phaseTicks(), duration) / duration) + "%";
        graphics.text(font, Component.literal(status), leftPos + 8, topPos + 63, 0xFF444444, false);
    }
}
