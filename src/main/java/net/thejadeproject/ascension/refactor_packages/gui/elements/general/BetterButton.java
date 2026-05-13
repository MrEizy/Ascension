package net.thejadeproject.ascension.refactor_packages.gui.elements.general;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyButton;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;

public class BetterButton extends EasyButton {
    public BetterButton(UIFrame frame, int x, int y) {
        super(frame, x, y);
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


}
