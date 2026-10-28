package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
    private final Button[] preparationButtons = new Button[5];
    private final int[] lastSelections = {-1, -1, -1, -1, -1};
    private static final String[] LABELS = {"Extract", "Heat", "Stabilize", "Condense", "Focus"};

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        titleLabelX = 8;
        titleLabelY = -5;
        inventoryLabelY = 73;
    }

    @Override
    protected void init() {
        super.init();
        int panelX = leftPos + imageWidth + 6;
        if (panelX + 146 > width) panelX = Math.max(3, leftPos - 151);
        for (int i = 0; i < preparationButtons.length; i++) {
            final int selection = i;
            lastSelections[i] = -1;
            preparationButtons[i] = addRenderableWidget(Button.builder(Component.literal(LABELS[i]), button -> {
                if (minecraft != null && minecraft.gameMode != null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selection);
            }).bounds(panelX, topPos + 8 + i * 24, 145, 20).build());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < preparationButtons.length; i++) {
            if (preparationButtons[i] == null) continue;
            preparationButtons[i].active = menu.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE.ordinal();
            if (minecraft == null || minecraft.player == null) continue;
            int selection = menu.preparationIndex(i);
            if (lastSelections[i] != selection) {
                lastSelections[i] = selection;
                preparationButtons[i].setMessage(Component.literal(LABELS[i] + ": "
                        + menu.selectionLabel(i, minecraft.player)));
                preparationButtons[i].setTooltip(Tooltip.create(Component.literal(menu.selectionDetails(i, minecraft.player))));
            }
        }
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
        if (phase == AlchemyFurnaceBlockEntity.Phase.IDLE) {
            int relativeX = mouseX - leftPos;
            int relativeY = mouseY - topPos;
            if (relativeX >= 72 && relativeX <= 101 && relativeY >= 30 && relativeY <= 58) {
                graphics.setTooltipForNextFrame(Component.literal("Pulse redstone to begin refinement"), mouseX, mouseY);
            }
        } else {
            graphics.text(font, Component.literal(status), leftPos + 8, topPos + 63, 0xFF444444, false);
        }
    }
}
