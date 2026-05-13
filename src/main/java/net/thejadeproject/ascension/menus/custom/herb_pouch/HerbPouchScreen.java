package net.thejadeproject.ascension.menus.custom.herb_pouch;

import net.lucent.easygui.screen.EasyContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.gui.elements.herb_pouch.HerbPouchElement;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch.ExtractHerbFromPouchPayload;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch.InsertCarriedHerbIntoPouchPayload;

import java.util.List;

public class HerbPouchScreen extends EasyContainerScreen<HerbPouchMenu> {
    private static final int HERB_START_X = 7;
    private static final int HERB_START_Y = 17;
    private static final int HERB_COLUMNS = 9;
    private static final int HERB_VISIBLE_ROWS = 3;

    public HerbPouchScreen(HerbPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        getUIFrame().setRoot(new HerbPouchElement(getUIFrame(), getMenu()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Vec2 root = getUIFrame().getRoot().getGlobalPoint();

        int relX = (int) root.x - this.leftPos;
        int relY = (int) root.y - this.topPos;

        guiGraphics.drawString(
                this.font,
                Component.translatable("item.ascension.herb_pouch"),
                relX + 8,
                relY + 6,
                0xE8D8A8,
                false
        );

        guiGraphics.drawString(
                this.font,
                this.playerInventoryTitle,
                relX + 8,
                relY + 35 + 18 * 2 + 1,
                0xE8D8A8,
                false
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHerbSummaries(guiGraphics, mouseX, mouseY);
    }

    private void renderHerbSummaries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Vec2 root = getUIFrame().getRoot().getGlobalPoint();
        int baseX = (int) root.x + HERB_START_X;
        int baseY = (int) root.y + HERB_START_Y;

        List<ItemStack> summaries = menu.getSummaryStacks();
        int visible = Math.min(summaries.size(), HERB_COLUMNS * HERB_VISIBLE_ROWS);

        for (int i = 0; i < visible; i++) {
            ItemStack stack = summaries.get(i);

            int x = baseX + (i % HERB_COLUMNS) * 18;
            int y = baseY + (i / HERB_COLUMNS) * 18;

            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);
        }

        for (int i = 0; i < visible; i++) {
            ItemStack stack = summaries.get(i);

            int x = baseX + (i % HERB_COLUMNS) * 18;
            int y = baseY + (i / HERB_COLUMNS) * 18;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInsideHerbArea(mouseX, mouseY) && !this.menu.getCarried().isEmpty()) {
            PacketDistributor.sendToServer(new InsertCarriedHerbIntoPouchPayload());
            return true;
        }

        Vec2 root = getUIFrame().getRoot().getGlobalPoint();
        int baseX = (int) root.x + HERB_START_X;
        int baseY = (int) root.y + HERB_START_Y;

        List<ItemStack> summaries = menu.getSummaryStacks();
        int visible = Math.min(summaries.size(), HERB_COLUMNS * HERB_VISIBLE_ROWS);

        for (int i = 0; i < visible; i++) {
            int x = baseX + (i % HERB_COLUMNS) * 18;
            int y = baseY + (i / HERB_COLUMNS) * 18;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                PacketDistributor.sendToServer(new ExtractHerbFromPouchPayload(i, hasShiftDown()));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInsideHerbArea(double mouseX, double mouseY) {
        Vec2 root = getUIFrame().getRoot().getGlobalPoint();

        int x = (int) root.x + HERB_START_X;
        int y = (int) root.y + HERB_START_Y;
        int width = HERB_COLUMNS * 18;
        int height = HERB_VISIBLE_ROWS * 18;

        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public Rect2i getUsedArea() {
        Vec2 point = getUIFrame().getRoot().getGlobalPoint();
        return new Rect2i(
                (int) point.x,
                (int) point.y,
                getUIFrame().getRoot().getWidth(),
                getUIFrame().getRoot().getHeight()
        );
    }
}