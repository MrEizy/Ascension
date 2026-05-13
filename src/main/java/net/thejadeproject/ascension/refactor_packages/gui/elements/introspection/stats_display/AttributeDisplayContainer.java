package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import org.checkerframework.checker.units.qual.C;

import java.text.DecimalFormat;

public class AttributeDisplayContainer extends RenderableElement {

    public DecimalFormat format = new DecimalFormat("0.##");
    public final Holder<Attribute> attributeHolder;
    private final ITextureData textureData;
    public EasyLabel valueLabel;
    private EasyLabel nameLabel;
    public AttributeDisplayContainer(UIFrame frame, Holder<Attribute> attribute, ITextureData textureData) {
        super(frame);
        this.attributeHolder = attribute;
        this.textureData = textureData;

        nameLabel = new EasyLabel(frame);
        nameLabel.setText(Component.empty());
        nameLabel.setTextColor(-1);
        nameLabel.setScaleToFit(true);
        nameLabel.setWidth(56-textureData.getWidth());
        nameLabel.getPositioning().setX(textureData.getWidth()+1);
        nameLabel.setHeight(6);

        valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.empty());
        valueLabel.setWidth(53);
        valueLabel.setHeight(6);
        valueLabel.setScaleToFit(true);
        valueLabel.setTextColor(-1);
        valueLabel.getPositioning().setY(textureData.getHeight()+1);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);

        AttributeValueContainer holder = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getAttribute(attributeHolder);
        if(holder == null) return;
        nameLabel.setText(holder.getDisplayName());


        addChild(nameLabel);
        addChild(valueLabel);
    }

    public void updateValue(){
        AttributeValueContainer holder = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getAttribute(attributeHolder);
        if(holder == null) return;
        valueLabel.setText(Component.literal(format.format(holder.getValue())));
    }

    @Override
    protected void run(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateValue();
        super.run(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(textureData != null) textureData.render(guiGraphics);
    }
}
