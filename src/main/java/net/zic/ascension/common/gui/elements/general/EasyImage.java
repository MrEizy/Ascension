package net.zic.ascension.common.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class EasyImage extends RenderableElement {
    private ITextureData textureData;

    public EasyImage(UIFrame frame, int length) {
        super(frame);
        setWidth(length);
        setHeight(length);
        getTransform().setUseScale(true);
    }

    public void setTextureData(ITextureData textureData) {
        this.textureData = textureData;
    }

    private float calculateScale() {
        if (textureData == null || textureData.getWidth() == 0) {
            return 1.0F;
        }
        return (float) getWidth() / textureData.getWidth();
    }

    @Override
    protected void run(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        getTransform().setScale(calculateScale());
        super.run(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (textureData != null) {
            textureData.render(graphics);
        }
    }
}
