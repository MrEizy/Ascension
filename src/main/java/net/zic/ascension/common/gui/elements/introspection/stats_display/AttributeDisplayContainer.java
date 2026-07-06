package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;

import java.text.DecimalFormat;

public class AttributeDisplayContainer extends RenderableElement {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final Holder<Attribute> attribute;
    private final ITextureData icon;
    private final EasyLabel valueLabel;

    private String displayedValue;

    public AttributeDisplayContainer(
            UIFrame frame,
            Holder<Attribute> attribute,
            ITextureData icon
    ) {
        super(frame);

        this.attribute = attribute;
        this.icon = icon;

        int iconWidth = icon == null ? 0 : icon.getWidth();
        int iconHeight = icon == null ? 0 : icon.getHeight();

        setWidth(56);
        setHeight(Math.max(13, iconHeight + 7));

        EasyLabel nameLabel = new EasyLabel(frame);
        nameLabel.setText(getDisplayName(attribute));
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setScaleToFit(true);
        nameLabel.setWidth(Math.max(1, getWidth() - iconWidth - 1));
        nameLabel.setHeight(6);
        nameLabel.getPositioning().setX(iconWidth + 1);
        nameLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(nameLabel);

        valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.literal("-"));
        valueLabel.setTextColor(0xFFFFFFFF);
        valueLabel.setScaleToFit(true);
        valueLabel.setWidth(53);
        valueLabel.setHeight(6);
        valueLabel.getPositioning().setY(iconHeight + 1);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        valueLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(valueLabel);

        updateValue();
    }

    private void updateValue() {
        String value = ClientAscensionData.getPlayer()
                .map(player -> {
                    ZenithAttributeHolder holder = player.getData(
                            ZenithAttachments.ATTRIBUTE_HOLDER
                    );

                    var zenithAttribute = holder.getAttribute(attribute);
                    if (zenithAttribute != null) {
                        return FORMAT.format(zenithAttribute.getValue());
                    }

                    if (player.getAttributes().hasAttribute(attribute)) {
                        return FORMAT.format(player.getAttributeValue(attribute));
                    }

                    return "-";
                })
                .orElse("-");

        if (value.equals(displayedValue)) {
            return;
        }

        displayedValue = value;
        valueLabel.setText(Component.literal(value));
        valueLabel.setTextScale(1.0F);
    }

    private static Component getDisplayName(Holder<Attribute> attribute) {
        if (attribute.equals(Attributes.MAX_HEALTH)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.health"
            );
        }

        if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.damage"
            );
        }

        if (attribute.equals(Attributes.ARMOR)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.armor"
            );
        }

        if (attribute.equals(Attributes.ARMOR_TOUGHNESS)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.toughness"
            );
        }

        if (attribute.equals(Attributes.ATTACK_SPEED)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.attack_speed"
            );
        }

        if (attribute.equals(Attributes.MOVEMENT_SPEED)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.speed"
            );
        }

        if (attribute.equals(Attributes.JUMP_STRENGTH)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.jump"
            );
        }

        if (attribute.equals(Attributes.STEP_HEIGHT)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.step_height"
            );
        }

        if (attribute.equals(Attributes.MINING_EFFICIENCY)) {
            return Component.translatable(
                    "gui.ascension.introspection.attribute.mining"
            );
        }

        return Component.translatable(
                attribute.value().getDescriptionId()
        );
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

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}