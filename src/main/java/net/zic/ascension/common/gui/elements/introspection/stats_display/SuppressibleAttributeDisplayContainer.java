package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedZenithAttribute;

public class SuppressibleAttributeDisplayContainer extends AttributeDisplayContainer {
    private final Holder<Attribute> attribute;

    public SuppressibleAttributeDisplayContainer(
            UIFrame frame,
            Holder<Attribute> attribute,
            ITextureData textureData
    ) {
        super(frame, attribute, textureData);
        this.attribute = attribute;

        SuppressedAttribute suppressedStat = new SuppressedAttribute(frame, attribute);
        suppressedStat.getPositioning().setY(18);
        addChild(suppressedStat);

        setHeight(28);
    }

    private boolean isSuppressed() {
        if(!Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).isSuppressable(attribute)) return false;

        return  ((SuppressedZenithAttribute)Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getAttribute(attribute))
                .getSuppression() < 1;
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (isSuppressed()) {
            graphics.fill(0, 0, getWidth(), getHeight(), 0x24FFAA44);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}