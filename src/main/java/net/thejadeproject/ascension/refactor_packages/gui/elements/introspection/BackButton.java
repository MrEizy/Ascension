package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyButton;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;

public class BackButton extends EasyButton {
    private final String screen;
    private boolean activeBtn;


    ITextureData defaultTexture;
    ITextureData altTexture;
    public BackButton(UIFrame frame) {
        super(frame, 0,0);
        this.screen = "main";
        ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                "textures/gui/main/back_buttons.png");
        defaultTexture = new TextureDataSubsection(
                textureIdentifier,
                18,21,
                0,0,
                18,10
        );
        altTexture = new TextureDataSubsection(
                textureIdentifier,
                18,21,
                0,12,
                18,10

        );
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());

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
    public void setActiveBtn(boolean state){
        this.activeBtn = state;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(activeBtn) altTexture.renderAt(guiGraphics,0,-2);
        else{
            if(isHovered() || isPressed()) altTexture.render(guiGraphics);
            else defaultTexture.render(guiGraphics);
        }
    }

    @Override
    public void onClick() {
        ((IntrospectionContainer) getParent().getParent()).openScreen(screen);
    }
}