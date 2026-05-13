package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.path_display;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyButton;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.IPath;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import org.checkerframework.checker.units.qual.C;

public class PathSelectionButton extends EasyButton {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_text_buttons.png"
    );
    ITextureData alternateTexture = new TextureDataSubsection(
            textureIdentifier,89,24,
            0,0,89,12
    );
    ITextureData defaultTexture = new TextureDataSubsection(
            textureIdentifier,89,24,
            0,12,89,12
    );
    public final ResourceLocation path;

    public PathSelectionButton(UIFrame frame,ResourceLocation path) {
        super(frame, 0,0);
        this.path = path;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
        EasyLabel label = new EasyLabel(frame);
        label.setTextColor(-1);
        label.setWidth(85);
        label.setHeight(8);
        label.getPositioning().setX(2);
        label.getPositioning().setY(2);
        label.setScaleToFit(true);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        if(path != null){
            IPath pathInstance = AscensionRegistries.Paths.PATHS_REGISTRY.get(path);
            label.setText(pathInstance.getDisplayTitle());
        }else label.setText(Component.empty());
        addChild(label);
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT,this::globalMouseMoveEvent);
    }
    public void globalMouseMoveEvent(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;

        if(isHovered() && !isPointBounded(easyMouseEvent.getMouseX(),easyMouseEvent.getMouseY())) setHovered(false);
    }
    @Override
    public void mouseMoveEvent(EasyEvent event){

        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return; //make sure the right event was called (should always be this but double check)

        setHovered(true);
        if(isPressed() && !isHovered()) setPressed(false); //if mouse moves away and was being pressed unPress
        event.setCanceled(true);
    }
    @Override
    public void onMouseDown(EasyEvent event){

        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;//make sure the right event was called (should always be this but double check)

        if(easyMouseEvent.button != InputConstants.MOUSE_BUTTON_LEFT) return; // check the correct button was used
        setPressed(true); //start pressing
    }
    @Override
    public void onMouseUp(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;//make sure the right event was called (should always be this but double check)

        if(easyMouseEvent.button != InputConstants.MOUSE_BUTTON_LEFT) return;// check the correct button was used
        if(isPressed()){//make sure mouse was pressed down over this element first
            //RUN CLICK LOGIC HERE
            onClick();
        }
        setPressed(false); //reset
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(isHovered() || isPressed()) alternateTexture.render(guiGraphics);
        else defaultTexture.render(guiGraphics);
    }
}
