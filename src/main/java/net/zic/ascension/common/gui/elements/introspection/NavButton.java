package net.zic.ascension.common.gui.elements.introspection;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class NavButton extends BetterButton {
    private final IntrospectionContainer owner;
    private final IntrospectionContainer.Panel panel;
    private final ITextureData defaultTexture;
    private final ITextureData alternateTexture;
    private boolean selected;

    public NavButton(
            UIFrame frame,
            IntrospectionContainer owner,
            IntrospectionContainer.Panel panel,
            Identifier texture
    ) {
        super(frame, 0, 0);
        this.owner = owner;
        this.panel = panel;
        this.defaultTexture = new TextureDataSubsection(texture, 24, 48, 0, 0, 24, 24);
        this.alternateTexture = new TextureDataSubsection(texture, 24, 48, 0, 24, 24, 24);
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick() {
        owner.openPanel(panel);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (selected) {
            alternateTexture.renderAt(graphics, 0, -2);
        } else if (isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
