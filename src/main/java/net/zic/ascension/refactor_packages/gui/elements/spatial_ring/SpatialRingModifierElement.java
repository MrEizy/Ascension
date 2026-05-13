package net.zic.ascension.refactor_packages.gui.elements.spatial_ring;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.refactor_packages.gui.elements.general.Container;
import net.zic.ascension.refactor_packages.gui.elements.general.PlayerInventoryContainer;

public class SpatialRingModifierElement extends RenderableElement {
    ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
            "textures/gui/spatial_rings/spatial_ring_upgrades.png");
    ITextureData bg = new TextureDataSubsection(
            textureLocation,
            256,256,
            0,0,
            176,222
    );
    public SpatialRingModifierElement(UIFrame frame) {
        super(frame);
        setWidth(bg.getWidth());
        setHeight(bg.getHeight());
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2);


        PlayerInventoryContainer playerInventoryContainer = new PlayerInventoryContainer(frame);
        playerInventoryContainer.getPositioning().setX(7);
        playerInventoryContainer.getPositioning().setY(139);
        addChild(playerInventoryContainer);

        Container spatialStoneContainer = new Container(frame,162,36);
        spatialStoneContainer.getPositioning().setX(7);
        spatialStoneContainer.getPositioning().setY(31);
        addChild(spatialStoneContainer);
        addSlots(36,spatialStoneContainer);

        Container upgradeContainer = new Container(frame,162,36);
        upgradeContainer.getPositioning().setX(7);
        upgradeContainer.getPositioning().setY(89);
        addChild(upgradeContainer);
        addSlots(54,upgradeContainer);

    }

    public void addSlots(int startSlot,RenderableElement parent){

        for(int row = 0;row<2;row++){
            for(int i =0;i<9;i++){
                RenderableElement element = new RenderableElement(getUiFrame());
                element.getPositioning().setX(1+i*18);
                element.getPositioning().setY(1+row*18);
                element.setWidth(16);
                element.setHeight(16);
                element.setId("slot_index_"+(startSlot+i+row*9));
                parent.addChild(element);
            }
        }
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
