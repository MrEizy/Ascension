package net.thejadeproject.ascension.refactor_packages.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphics;

public class EasyImage extends RenderableElement {
    private ITextureData textureData;
    public EasyImage(UIFrame frame,int length) {
        super(frame);
        setWidth(length);
        setHeight(length);
        getTransform().setUseScale(true);
    }


    public void setTextureData(ITextureData textureData){
        this.textureData = textureData;
    }

    //assumes square
    public float updateScaleFactor(){
        if(textureData == null) return 1;
        return (float) getWidth()/textureData.getWidth();
    }

    @Override
    protected void run(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getTransform().setScale(updateScaleFactor());
        super.run(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(textureData != null) textureData.render(guiGraphics);
    }
}
