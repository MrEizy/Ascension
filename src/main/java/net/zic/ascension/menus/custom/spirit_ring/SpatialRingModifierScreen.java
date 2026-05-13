package net.zic.ascension.menus.custom.spirit_ring;

import net.lucent.easygui.screen.EasyContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec2;
import net.zic.ascension.refactor_packages.gui.elements.spatial_ring.SpatialRingModifierElement;

public class SpatialRingModifierScreen extends EasyContainerScreen<SpatialRingModifierMenu> {
    public SpatialRingModifierScreen(SpatialRingModifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        getUIFrame().setRoot(new SpatialRingModifierElement(getUIFrame()));
    }
    public Rect2i getUsedArea(){
        Vec2 point = getUIFrame().getRoot().getGlobalPoint();
        return new Rect2i((int) point.x, (int) point.y,getUIFrame().getRoot().getWidth(),getUIFrame().getRoot().getHeight());
    }
}
