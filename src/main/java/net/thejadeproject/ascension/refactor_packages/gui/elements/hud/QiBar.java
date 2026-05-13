package net.thejadeproject.ascension.refactor_packages.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;

import java.text.DecimalFormat;

public class QiBar extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/qi_bar.png"
    );
    ITextureData textureData = new TextureData(
            textureIdentifier,85,9);
    DecimalFormat format = new DecimalFormat("#.0");

    public QiBar(UIFrame frame) {
        super(frame);
        setWidth(textureData.getWidth());
        setHeight(textureData.getHeight());
    }

    public double getProgress(){

        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        EntityQiContainer entityQiContainer = entityData.getQiContainer();
        if(getChildren().isEmpty()){
            EasyLabel label = new EasyLabel(getUiFrame());
            addChild(label);
            label.setWidth(50);
            label.setHeight(getHeight());

            label.getPositioning().setXPositioningRule(PositioningRules.CENTER);
            label.getPositioning().setX(-label.getWidth()/2);
            label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
            label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
            label.setTextColor(-1);
            label.setScaleToFit(true);
        }

        EasyLabel label = (EasyLabel) getChildren().getFirst();
        label.setText(Component.literal(
                format.format(entityQiContainer.getCurrentQi())+"/"+
                        format.format(entityQiContainer.getMaxQi())));

        return entityQiContainer.getCurrentQi()/entityQiContainer.getMaxQi();

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(getProgress() != 0){
            //System.out.println(getProgress());
            textureData.render(guiGraphics, (int) ((getWidth())*getProgress()),getHeight());
        }
    }
}
