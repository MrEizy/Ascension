package net.thejadeproject.ascension.refactor_packages.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class HealthBar extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/hp_bar.png"
    );
    ITextureData textureData = new TextureData(
            textureIdentifier,101,9);
    public HealthBar(UIFrame frame) {
        super(frame);
        setWidth(textureData.getWidth());
        setHeight(textureData.getHeight());

    }

    DecimalFormat format = new DecimalFormat("#.0");

    public double getProgress(){
        Player player = Minecraft.getInstance().player;
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);

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

        if(getAbsorptionProgress() != 0)label.setText(Component.literal(
                format.format(player.getHealth())+"+("+ format.format(+player.getAbsorptionAmount())+")"+"/"+
                        format.format(player.getMaxHealth())));
        else label.setText(Component.literal(
                format.format(player.getHealth())+"/"+
                        format.format(player.getMaxHealth())));

        return Math.clamp(entityData.getHealth()/player.getMaxHealth(),0,1);

    }
    public double getAbsorptionProgress(){
        Player player = Minecraft.getInstance().player;
        return Math.clamp(player.getAbsorptionAmount()/player.getMaxHealth(),0,1);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        //render background
        if(getProgress() != 0){
            textureData.render(guiGraphics, (int) ((getWidth())*getProgress()),getHeight());
        }
        //guiGraphics.fill(0,0,getWidth(),getHeight(),0xFF000000);
        //if(getProgress() != 0) guiGraphics.fill(1,1, (int) ((getWidth()-1)*getProgress()),getHeight()-1,0xFFFA0000);
        //if(getAbsorptionProgress() != 0) guiGraphics.fill(1,1, (int) ((getWidth()-1)*getAbsorptionProgress()),getHeight()-1,0xFFFFBB00);
    }
}
