package net.zic.ascension.common.gui.elements.introspection;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public class ActiveMenuTitle extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE,
            256,
            260,
            98,
            190,
            106,
            39
    );

    private final EasyLabel label;

    public ActiveMenuTitle(UIFrame frame) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());

        label = new EasyLabel(frame);
        label.setText(Component.empty());
        label.setWidth(55);
        label.setHeight(8);
        label.getPositioning().setX(11);
        label.getPositioning().setY(16);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        label.setScaleToFit(true);
        label.setTextColor(0xFFFFFFFF);
        addChild(label);
    }

    public void setMenuName(Component name) {
        label.setText(name == null ? Component.empty() : name);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
