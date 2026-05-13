package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;

public class ActiveMenuTitle extends RenderableElement {

    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            256,260,
            98,190,
            106,39
    );
    EasyLabel label;
    public ActiveMenuTitle(UIFrame frame) {
        super(frame);
        label = new EasyLabel(frame);
        label.setWidth(55);
        label.setHeight(8);
        label.getPositioning().setX(11);
        label.getPositioning().setY(16);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setScaleToFit(true);
        label.setText(Component.empty());
        label.setTextColor(-1);
        addChild(label);
    }

    public void setMenuName(Component name){
        label.setText(name);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
