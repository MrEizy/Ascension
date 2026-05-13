package net.thejadeproject.ascension.menus.custom.spirit_ring;

import net.lucent.easygui.screen.EasyContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec2;
import net.thejadeproject.ascension.refactor_packages.gui.elements.spatial_ring.SpatialRingInventoryElement;

public class SpatialRingInventoryScreen extends EasyContainerScreen<SpatialRingInventoryMenu> {
    private final SpatialRingInventoryElement rootElement;

    public SpatialRingInventoryScreen(SpatialRingInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.rootElement = new SpatialRingInventoryElement(getUIFrame(), getMenu());

        getUIFrame().setRoot(this.rootElement);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Vec2 root = getUIFrame().getRoot().getGlobalPoint();

        int relX = (int) root.x - this.leftPos;
        int relY = (int) root.y - this.topPos;

        int extraRows = rootElement.getExtraRows();

        guiGraphics.drawString(
                this.font,
                Component.translatable("item.ascension.spatial_ring"),
                relX + 8,
                relY + 6,
                0xE8D8A8,
                false
        );

        guiGraphics.drawString(
                this.font,
                this.playerInventoryTitle,
                relX + 8,
                relY + 35 + 18 * extraRows + 1,
                0xE8D8A8,
                false
        );
    }


    public Rect2i getUsedArea(){
        Vec2 point = getUIFrame().getRoot().getGlobalPoint();
        return new Rect2i((int) point.x, (int) point.y,getUIFrame().getRoot().getWidth(),getUIFrame().getRoot().getHeight());
    }
}
