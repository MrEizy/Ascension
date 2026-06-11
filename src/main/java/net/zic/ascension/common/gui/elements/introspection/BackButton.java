package net.zic.ascension.common.gui.elements.introspection;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class BackButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/back_buttons.png"
    );

    private final IntrospectionContainer owner;
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 18, 21, 0, 0, 18, 10
    );
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 18, 21, 0, 12, 18, 10
    );

    public BackButton(UIFrame frame, IntrospectionContainer owner) {
        super(frame, 0, 0);
        this.owner = owner;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
    }

    @Override
    public void onClick() {
        owner.openPanel(IntrospectionContainer.Panel.MAIN);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
