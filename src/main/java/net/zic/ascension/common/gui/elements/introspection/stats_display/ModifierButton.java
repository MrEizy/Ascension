package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class ModifierButton extends BetterButton {
    private final ITextureData texture;

    public ModifierButton(UIFrame frame, int x, int y, ITextureData texture) {
        super(frame, x, y);
        this.texture = texture;
        setWidth(8);
        setHeight(8);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (texture != null) {
            texture.render(graphics);
        }

        if (isHovered()) {
            graphics.fill(0, 0, getWidth(), getHeight(), 0xB4999999);
        }
    }
}