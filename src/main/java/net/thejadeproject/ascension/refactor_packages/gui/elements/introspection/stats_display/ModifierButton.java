package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphics;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.BetterButton;

public class ModifierButton extends BetterButton {
    ITextureData textureData;
    public ModifierButton(UIFrame frame, int x, int y, ITextureData textureData) {
        super(frame, x, y);
        this.textureData = textureData;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(textureData != null) textureData.render(guiGraphics);

        if(isHovered()) guiGraphics.fill(0,0,getWidth(),getHeight(),-1265265259);
    }
}
