package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.main;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyButton;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;

public class BloodlineOpenButton extends EasyButton {

    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/text_buttons.png"
    );

    ITextureData alternateTexture = new TextureDataSubsection(
            textureIdentifier, 89, 24,
            0, 0, 89, 12
    );

    ITextureData defaultTexture = new TextureDataSubsection(
            textureIdentifier, 89, 24,
            0, 12, 89, 12
    );

    public BloodlineOpenButton(UIFrame frame) {
        super(frame, 0, 0);

        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());

        IBloodline bloodline = Minecraft.getInstance().player
                .getData(ModAttachments.ENTITY_DATA)
                .getBloodline();

        EasyLabel title = new EasyLabel(frame);
        title.getPositioning().setX(2);
        title.getPositioning().setY(2);
        title.setWidth(85);
        title.setHeight(8);
        title.setScaleToFit(true);
        title.setTextColor(-1);
        title.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        title.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);

        if (bloodline != null && bloodline.getDisplayTitle() != null) {
            title.setText(bloodline.getDisplayTitle());
        } else {
            title.setText(Component.literal("No Bloodline"));
        }

        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT, this::globalMouseMoveEvent);
        addChild(title);
    }

    public void globalMouseMoveEvent(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent easyMouseEvent)) return;

        if (isHovered() && !isPointBounded(easyMouseEvent.getMouseX(), easyMouseEvent.getMouseY())) {
            setHovered(false);
        }
    }

    @Override
    public void mouseMoveEvent(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent)) return;

        setHovered(true);

        if (isPressed() && !isHovered()) {
            setPressed(false);
        }

        event.setCanceled(true);
    }

    @Override
    public void onMouseDown(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent easyMouseEvent)) return;

        if (easyMouseEvent.button != InputConstants.MOUSE_BUTTON_LEFT) return;

        setPressed(true);
    }

    @Override
    public void onMouseUp(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent easyMouseEvent)) return;

        if (easyMouseEvent.button != InputConstants.MOUSE_BUTTON_LEFT) return;

        if (isPressed()) {
            onClick();
        }

        setPressed(false);
    }

    @Override
    public void onClick() {
        ((MainContainer) getParent()).displayBloodline();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered() || isPressed()) {
            alternateTexture.render(guiGraphics);
        } else {
            defaultTexture.render(guiGraphics);
        }
    }
}