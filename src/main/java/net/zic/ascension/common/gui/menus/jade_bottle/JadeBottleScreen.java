package net.zic.ascension.common.gui.menus.jade_bottle;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.zic.ascension.common.gui.elements.jade_bottle.JadeBottleInventoryElement;

public class JadeBottleScreen extends AbstractContainerScreen<JadeBottleMenu> {

    private UIFrame frame;
    private JadeBottleInventoryElement rootElement;

    public JadeBottleScreen(JadeBottleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public int getImageWidth() {
        return 176;
    }

    @Override
    public int getImageHeight() {
        return 138;
    }

    @Override
    protected void init() {
        super.init();
        this.frame = new UIFrame();
        this.rootElement = new JadeBottleInventoryElement(frame, menu);
        frame.setRoot(rootElement);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        frame.run(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (frame.mouseClicked(event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }
}
