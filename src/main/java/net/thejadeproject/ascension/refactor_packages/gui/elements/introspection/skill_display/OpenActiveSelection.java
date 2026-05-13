package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display;

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

public class OpenActiveSelection extends EasyButton {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );
    ITextureData openDefaultTexture = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            187,241,13,11
    );
    ITextureData openAlternateTexture = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            187,252,13,11
    );
    ITextureData closeDefaultTexture = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            174,241,13,11
    );
    ITextureData closeAlternateTexture = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            174,252,13,11
    );
    private boolean open;
    public OpenActiveSelection(UIFrame frame, int x, int y) {
        super(frame, x, y);
        setWidth(openDefaultTexture.getWidth());
        setHeight(openDefaultTexture.getHeight());
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
    public boolean isOpen(){return open;}
    @Override
    public void onClick() {
        super.onClick();
        open = !open;
        if(getUiFrame().getElementById("skill_bar_container") instanceof SkillBarContainer container){
            if(open)container.open();
            else container.setActive(false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(isOpen()){
            if(isHovered()) closeAlternateTexture.render(guiGraphics);
            else closeDefaultTexture.render(guiGraphics);
        }else{
            if(isHovered()) openAlternateTexture.render(guiGraphics);
            else openDefaultTexture.render(guiGraphics);
        }
    }
}
