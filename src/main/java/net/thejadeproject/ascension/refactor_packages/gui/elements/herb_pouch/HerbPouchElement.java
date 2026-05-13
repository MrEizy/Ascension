package net.thejadeproject.ascension.refactor_packages.gui.elements.herb_pouch;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.PlayerInventoryContainer;

public class HerbPouchElement extends RenderableElement {
    private final ResourceLocation textureLocation =
            ResourceLocation.fromNamespaceAndPath(
                    AscensionCraft.MOD_ID,
                    "textures/gui/spatial_rings/spatial_ring.png"
            );

    private final ITextureData topBgElement = new TextureDataSubsection(
            textureLocation,
            256, 256,
            0, 95,
            176, 35
    );

    private final ITextureData rowBgElement = new TextureDataSubsection(
            textureLocation,
            256, 256,
            0, 112,
            176, 18
    );

    private final ITextureData playerInventoryBgElement = new TextureDataSubsection(
            textureLocation,
            256, 256,
            0, 0,
            176, 96
    );

    private static final int HERB_ROWS = 2;

    public HerbPouchElement(UIFrame frame, HerbPouchMenu menu) {
        super(frame);

        getPositioning().setPositioningRule(PositioningRules.CENTER);
        setWidth(176);
        setHeight(36 + 96 + HERB_ROWS * 18);

        PlayerInventoryContainer playerInventoryContainer = new PlayerInventoryContainer(frame);
        playerInventoryContainer.getPositioning().setX(7);
        playerInventoryContainer.getPositioning().setY(35 + 18 * HERB_ROWS + 13);
        addChild(playerInventoryContainer);

        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        topBgElement.render(guiGraphics);

        for (int i = 0; i < HERB_ROWS; i++) {
            rowBgElement.renderAt(guiGraphics, 0, 35 + 18 * i);
        }

        playerInventoryBgElement.renderAt(guiGraphics, 0, 35 + 18 * HERB_ROWS);
    }
}