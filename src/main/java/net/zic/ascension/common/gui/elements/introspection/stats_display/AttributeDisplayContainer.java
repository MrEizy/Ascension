package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.ascension.common.gui.data.ClientAscensionData;

import java.text.DecimalFormat;

public class AttributeDisplayContainer extends RenderableElement {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final Holder<Attribute> attribute;
    private final ITextureData icon;
    private final EasyLabel valueLabel;

    public AttributeDisplayContainer(
            UIFrame frame,
            Holder<Attribute> attribute,
            ITextureData icon
    ) {
        super(frame);
        this.attribute = attribute;
        this.icon = icon;
        setWidth(56);
        setHeight(Math.max(16, icon == null ? 16 : icon.getHeight() + 8));

        int iconWidth = icon == null ? 0 : icon.getWidth();

        EasyLabel nameLabel = new EasyLabel(frame);
        nameLabel.setText(Component.translatable(attribute.value().getDescriptionId()));
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setWidth(Math.max(1, getWidth() - iconWidth - 1));
        nameLabel.setHeight(7);
        nameLabel.setScaleToFit(true);
        nameLabel.getPositioning().setX(iconWidth + 1);
        nameLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(nameLabel);

        valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.literal("-"));
        valueLabel.setWidth(getWidth());
        valueLabel.setHeight(7);
        valueLabel.setScaleToFit(true);
        valueLabel.setTextColor(0xFFFFFFFF);
        valueLabel.getPositioning().setY((icon == null ? 7 : icon.getHeight()) + 1);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        valueLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(valueLabel);
    }

    private void updateValue() {
        Component value = ClientAscensionData.getPlayer()
                .filter(player -> player.getAttributes().hasAttribute(attribute))
                .map(player -> Component.literal(FORMAT.format(
                        player.getAttributeValue(attribute)
                )))
                .orElseGet(() -> Component.literal("-"));
        valueLabel.setTextScale(1.0F);
        valueLabel.setText(value);
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateValue();
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (icon != null) {
            icon.render(graphics);
        }
    }
}
