package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.ascension.common.gui.data.ClientAscensionData;

public class SuppressibleAttributeDisplayContainer extends AttributeDisplayContainer {
    private final Holder<Attribute> attribute;

    public SuppressibleAttributeDisplayContainer(
            UIFrame frame,
            Holder<Attribute> attribute,
            ITextureData textureData
    ) {
        super(frame, attribute, textureData);
        this.attribute = attribute;

        SuppressedStat suppressedStat = new SuppressedStat(frame, attribute);
        suppressedStat.getPositioning().setY(18);
        addChild(suppressedStat);

        setHeight(28);
    }

    private boolean isSuppressed() {
        return ClientAscensionData.getEntityData()
                .map(data -> data.getAttributeSuppression(attribute) < 1.0D)
                .orElse(false);
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