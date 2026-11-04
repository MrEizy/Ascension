package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
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
    private static final int PANEL_WIDTH = 142;
    private static final int BOX_HEIGHT = 22;
    private static final int BOX_STEP = 26;
    private static final String[] LABELS = {"Extraction", "Heating", "Stabilization", "Condensation", "Medicinal focus"};

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        titleLabelX = 8;
        titleLabelY = -5;
        inventoryLabelY = 73;
    }

    private int panelX() {
        int right = leftPos + imageWidth + 6;
        if (right + PANEL_WIDTH <= width - 3) return right;
        int left = leftPos - PANEL_WIDTH - 6;
        return left >= 3 ? left : Math.max(3, width - PANEL_WIDTH - 3);
    }

    private int boxY(int index) {
        return topPos + 22 + index * BOX_STEP;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && minecraft != null && minecraft.gameMode != null
                && menu.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE.ordinal()) {
            int x = panelX();
            for (int index = 0; index < LABELS.length; index++) {
                int y = boxY(index);
                if (event.x() >= x && event.x() < x + PANEL_WIDTH
                        && event.y() >= y && event.y() < y + BOX_HEIGHT) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
                0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int ordinal = Math.max(0, Math.min(AlchemyFurnaceBlockEntity.Phase.values().length - 1, menu.phase()));
        AlchemyFurnaceBlockEntity.Phase phase = AlchemyFurnaceBlockEntity.Phase.values()[ordinal];
        int duration = AlchemyFurnaceBlockEntity.duration(phase);
        String label = switch (phase) {
            case IDLE -> "Ready";
            case EXTRACTION -> "Extract";
            case REACTION -> "React";
            case STABILIZATION -> "Stabilize";
            case CONDENSATION -> "Condense";
            case SUCCESS -> "Complete";
            case FAILURE -> "Failed";
        };
        String status = switch (phase) {
            case EXTRACTION, REACTION, STABILIZATION, CONDENSATION ->
                    label + " " + (100 * Math.min(menu.phaseTicks(), duration) / duration) + "%";
            default -> label;
        };
        if (phase == AlchemyFurnaceBlockEntity.Phase.IDLE) {
            int relativeX = mouseX - leftPos;
            int relativeY = mouseY - topPos;
            if (relativeX >= 72 && relativeX <= 101 && relativeY >= 30 && relativeY <= 58) {
                graphics.setTooltipForNextFrame(Component.literal(
                        "Sneak + right-click the furnace with an empty hand to start refinement"), mouseX, mouseY);
            }
        } else {
            graphics.text(font, Component.literal(status), leftPos + 8, topPos + 63, 0xFF444444, false);
        }

        int x = panelX();
        graphics.fill(x - 2, topPos + 4, x + PANEL_WIDTH + 2, boxY(4) + BOX_HEIGHT + 4, 0xDE171C23);
        graphics.text(font, Component.literal("Preparation"), x + 4, topPos + 9, 0xFFE1D4BA, false);
        for (int i = 0; i < LABELS.length; i++) {
            int y = boxY(i);
            boolean hovered = mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + BOX_HEIGHT;
            boolean editable = phase == AlchemyFurnaceBlockEntity.Phase.IDLE;
            int border = editable && hovered ? 0xFFCAA76C : 0xFF5C626B;
            graphics.fill(x, y, x + PANEL_WIDTH, y + BOX_HEIGHT, border);
            graphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + BOX_HEIGHT - 1,
                    editable ? 0xFF333945 : 0xFF262A30);
            graphics.text(font, Component.literal(LABELS[i]), x + 5, y + 2, 0xFFAAB4C2, false);
            String value = menu.selectionLabel(i, minecraft == null ? null : minecraft.player);
            if (font.width(value) > PANEL_WIDTH - 12)
                value = font.plainSubstrByWidth(value, PANEL_WIDTH - 18) + "…";
            graphics.text(font, Component.literal(value), x + 5, y + 12, 0xFFF4E7CD, false);
            if (hovered && minecraft != null && minecraft.player != null) {
                String detail = menu.selectionDetails(i, minecraft.player);
                if (!editable) detail = "Cannot change preparation during refinement. " + detail;
                graphics.setTooltipForNextFrame(Component.literal(detail), mouseX, mouseY);
            }
        }
    }
}
