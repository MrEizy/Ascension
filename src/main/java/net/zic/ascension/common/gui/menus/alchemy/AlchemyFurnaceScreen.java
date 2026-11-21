package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;

import java.util.List;

public class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {
    private static final Identifier TEXTURE = AscensionCraft.prefix("textures/gui/pill_cauldron_low_human/pill_cauldron_low_human_menu.png");
    private static final int PREVIEW_SLOT_X = 69;
    private static final int PREVIEW_SLOT_Y = 59;
    private int previewPage;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 142;
    private static final int BOX_HEIGHT = 22;
    private static final int BOX_STEP = 26;
    private static final String[] LABELS = {"Extraction", "Heating", "Stabilization", "Condensation", "Medicinal focus"};

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        titleLabelX = 8;
        titleLabelY = 3;
        inventoryLabelY = 74;
    }

    private int panelX() {
        int left = leftPos - PANEL_WIDTH - 6;
        if (left >= 3) return left;
        int right = leftPos + imageWidth + 6;
        return right + PANEL_WIDTH <= width - 3 ? right : Math.max(3, width - PANEL_WIDTH - 3);
    }

    private int boxY(int index) {
        return topPos + 22 + index * BOX_STEP;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (menu.hasPillPrediction() && menu.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE.ordinal()
                && (event.button() == 0 || event.button() == 1)
                && event.x() >= leftPos + PREVIEW_SLOT_X - 1 && event.x() < leftPos + PREVIEW_SLOT_X + 17
                && event.y() >= topPos + PREVIEW_SLOT_Y - 1 && event.y() < topPos + PREVIEW_SLOT_Y + 17
                && minecraft != null) {
            List<ItemStack> predictions = menu.predictedPills(minecraft.player);
            if (!predictions.isEmpty()) previewPage = Math.floorMod(previewPage + (event.button() == 0 ? 1 : -1), predictions.size());
            return true;
        }
        if ((event.button() == 0 || event.button() == 1) && minecraft != null && minecraft.gameMode != null
                && menu.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE.ordinal()) {
            int x = panelX();
            for (int index = 0; index < LABELS.length; index++) {
                int y = boxY(index);
                if (event.x() >= x && event.x() < x + PANEL_WIDTH
                        && event.y() >= y && event.y() < y + BOX_HEIGHT) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index + (event.button() == 0 ? 5 : 0));
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

        if (menu.hasPillPrediction() && menu.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE.ordinal()
                && minecraft != null && minecraft.player != null) {
            List<ItemStack> predictions = menu.predictedPills(minecraft.player);
            if (!predictions.isEmpty()) {
                previewPage = Math.floorMod(previewPage, predictions.size());
                ItemStack ghost = predictions.get(previewPage);
                int px = leftPos + PREVIEW_SLOT_X;
                int py = topPos + PREVIEW_SLOT_Y;
                graphics.item(ghost, px, py);
                graphics.fill(px, py, px + 16, py + 16, 0x50B1E8FF);
                graphics.text(font, Component.literal(previewPage == 0 ? "Predicted" : "Alternate"),
                        leftPos + 7, topPos + 63, 0xFF49788D, false);
                if (mouseX >= leftPos + 7 && mouseX <= leftPos + 55
                        && mouseY >= topPos + 62 && mouseY <= topPos + 74) {
                    graphics.setTooltipForNextFrame(Component.literal(
                            "A best-case prediction: assumes sufficient reaction Qi and successful stabilization. "
                            + "Other outcomes require different focus or refinement conditions. "
                            + "Click the ghost pill to switch predictions."), mouseX, mouseY);
                }
                if (predictions.size() > 1) {
                    graphics.text(font, (previewPage + 1) + "/" + predictions.size(), px - 1, py + 15, 0xFF6AD9ED, true);
                }
                if (mouseX >= px - 1 && mouseX < px + 17 && mouseY >= py - 1 && mouseY < py + 17) {
                    graphics.setTooltipForNextFrame(font, ghost, mouseX, mouseY);
                }
            }
        }

        int ordinal = Math.max(0, Math.min(AlchemyFurnaceBlockEntity.Phase.values().length - 1, menu.phase()));
        AlchemyFurnaceBlockEntity.Phase phase = AlchemyFurnaceBlockEntity.Phase.values()[ordinal];
        int duration = menu.phaseDuration(phase);
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
        if (phase == AlchemyFurnaceBlockEntity.Phase.REACTION) {
            status += " | Qi " + menu.reactionQiProgress() + "%";
        } else if (phase == AlchemyFurnaceBlockEntity.Phase.STABILIZATION) {
            status += " | Score " + menu.stabilizationProgress() + "%";
        }
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
                else detail = "Left-click: previous. Right-click: next. " + detail;
                graphics.setTooltipForNextFrame(font, font.split(Component.literal(detail), Math.min(220, Math.max(100, width - 32))), mouseX, mouseY);
            }
        }
    }
}
