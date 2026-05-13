package net.thejadeproject.ascension.refactor_packages.gui.elements.spatial_ring;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.menus.custom.spirit_ring.SpatialRingInventoryMenu;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.PlayerInventoryContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollItemContainer;

public class SpatialRingInventoryElement extends RenderableElement {
    ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/spatial_rings/spatial_ring.png");

    ITextureData topBgElement = new TextureDataSubsection(
            textureLocation,
            256,256,
            0,95,
            176,35
    );
    ITextureData rowBgElement = new TextureDataSubsection(
            textureLocation,
            256,256,
            0,112,
            176,18
    );
    ITextureData playerInventoryBgElement = new TextureDataSubsection(
            textureLocation,
            256,256,
            0,0,
            176,96
    );
    private final int extraRows;
    public SpatialRingInventoryElement(UIFrame frame, SpatialRingInventoryMenu menu) {
        super(frame);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        setWidth(176);
        setHeight(36+96);//this is the min height, change with spirit ring inventory size

        //TODO add spirit ring inventory
        int startSlot = 36;
        int totalSlots = menu.getSpatialRingInventorySlots();
        ScrollItemContainer spatialRingItemContainer = new ScrollItemContainer(frame,6,startSlot,startSlot+totalSlots-1);
        extraRows = Math.min((spatialRingItemContainer.getHeight() - 18) / 18, 5);

        spatialRingItemContainer.getPositioning().setY(17);
        spatialRingItemContainer.getPositioning().setX(7);
        addChild(spatialRingItemContainer);
        setHeight(36+96+extraRows*18);
        PlayerInventoryContainer playerInventoryContainer = new PlayerInventoryContainer(frame);

        //TODO updatePositioning to match generated rows
        playerInventoryContainer.getPositioning().setY(35+18*extraRows+13);
        playerInventoryContainer.getPositioning().setX(7);

        addChild(playerInventoryContainer);
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        topBgElement.render(guiGraphics);
        for(int i =0;i<extraRows;i++){
            rowBgElement.renderAt(guiGraphics,0,35+18*i);
        }
        playerInventoryBgElement.renderAt(guiGraphics,0,35+18*extraRows);
    }

    public int getExtraRows() {
        return extraRows;
    }
}
