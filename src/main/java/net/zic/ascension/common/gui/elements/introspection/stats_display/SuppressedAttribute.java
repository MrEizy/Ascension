package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.network.UpdateAttributeSuppressionPacket;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedZenithAttribute;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

public class SuppressedAttribute extends RenderableElement {
    private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/stats_menu/stats_menu.png"
    );

    private final Holder<Attribute> attribute;
    private final BetterButton minusButton;
    private final BetterButton plusButton;
    private final EasyLabel label;

    public SuppressedAttribute(UIFrame frame, Holder<Attribute> attribute) {
        super(frame);
        this.attribute = attribute;

        setWidth(56);
        setHeight(10);

        minusButton = new ModifierButton(
                frame,
                2,
                0,
                new TextureDataSubsection(TEXTURE, 234, 236, 29, 195, 8, 8)
        );
        addChild(minusButton);

        plusButton = new ModifierButton(
                frame,
                46,
                0,
                new TextureDataSubsection(TEXTURE, 234, 236, 38, 195, 8, 8)
        );
        addChild(plusButton);

        label = new EasyLabel(frame);
        label.setText(Component.literal("100%"));
        label.setTextColor(0xFFFFFFFF);
        label.setScaleToFit(true);
        label.getPositioning().setX(11);
        label.setWidth(34);
        label.setHeight(6);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(label);

        addEventListener(EasyEvents.MOUSE_UP_EVENT, this::onMouseUp);
    }

    private void onMouseUp(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }

        MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
        if (buttonEvent == null) {
            return;
        }

        double amount = 0.0D;

        if (event.getTarget() == minusButton) {
            amount = -getStep(buttonEvent);
        }

        if (event.getTarget() == plusButton) {
            amount = getStep(buttonEvent);
        }

        if (amount == 0.0D) {
            return;
        }

        adjust(amount);
    }

    private double getStep(MouseButtonEvent buttonEvent) {
        int modifiers = buttonEvent.modifiers();

        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            return 0.001D;
        }

        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            return 0.01D;
        }

        return 0.1D;
    }

    private void adjust(double amount) {
        Identifier attributeId = SimpleAscensionEntityData.getAttributeId(attribute);
        if (attributeId == null) {
            return;
        }

        double current = getPercentage();
        double next = Math.clamp(current + amount, 0.001D, 1.0D);

        if (next == current) {
            return;
        }

        ClientPacketDistributor.sendToServer(
                new UpdateAttributeSuppressionPacket(attributeId, next)
        );

        Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).setSuppression(attribute,next);

    }

    private double getPercentage() {
        return ((SuppressedZenithAttribute)Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getAttribute(attribute))
                .getSuppression();
    }

    private double getActualAttributeValue() {
        return ((SuppressedZenithAttribute)Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getAttribute(attribute))
                .getUnsuppressedValue();
    }

    private double getSuppressedAttributeValue() {
        return Minecraft.getInstance().player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getAttribute(attribute).getValue();
    }

    private void updatePercentage() {
        double percentage = getPercentage();

        if (percentage >= 1.0D) {
            label.setText(Component.literal("100%"));
            label.setTextColor(0xFFFFFFFF);
            label.setTextScale(1.0F);
            return;
        }

        double suppressedValue = getSuppressedAttributeValue();
        String percentageText = PERCENT_FORMAT.format(percentage * 100.0D) + "%";

        if (Double.isNaN(suppressedValue)) {
            label.setText(Component.literal(percentageText));
        } else {
            label.setText(Component.literal(
                    VALUE_FORMAT.format(suppressedValue) + " / " + percentageText
            ));
        }

        label.setTextColor(0xFFFFAA44);
        label.setTextScale(1.0F);
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updatePercentage();
    }
}