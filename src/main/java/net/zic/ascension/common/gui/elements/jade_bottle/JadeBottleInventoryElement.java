package net.zic.ascension.common.gui.elements.jade_bottle;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.menus.jade_bottle.JadeBottleMenu;

public class JadeBottleInventoryElement extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/jade_bottles/jade_bottle.png"
    );

    private static final int TEX_WIDTH = 176;
    private static final int TEX_HEIGHT = 138;
    private static final int PLAYER_INV_Y = 55;

    ITextureData topBgElement = new TextureDataSubsection(TEXTURE, TEX_WIDTH, TEX_HEIGHT, 0, 0, 176, PLAYER_INV_Y);
    ITextureData playerInventoryBgElement = new TextureDataSubsection(TEXTURE, TEX_WIDTH, TEX_HEIGHT, 0, PLAYER_INV_Y, 176, TEX_HEIGHT - PLAYER_INV_Y);

    public JadeBottleInventoryElement(UIFrame frame, JadeBottleMenu menu) {
        super(frame);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        setWidth(TEX_WIDTH);
        setHeight(TEX_HEIGHT);

        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        topBgElement.render(guiGraphics);
        playerInventoryBgElement.renderAt(guiGraphics, 0, PLAYER_INV_Y);
    }
}
