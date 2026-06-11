package net.zic.ascension.common.gui.elements.general;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyButton;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class BetterButton extends EasyButton {
    public BetterButton(UIFrame frame, int x, int y) {
        super(frame, x, y);
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT, this::onGlobalMouseMove);
    }

    private void onGlobalMouseMove(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        if (!isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY())) {
            setHovered(false);
            setPressed(false);
        }
    }

    @Override
    public void mouseMoveEvent(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        setHovered(isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY()));
        if (!isHovered()) {
            setPressed(false);
        }
    }

    @Override
    public void onMouseDown(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
        if (buttonEvent == null || buttonEvent.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }
        if (isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY())) {
            setPressed(true);
        }
    }

    @Override
    public void onMouseUp(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
        if (buttonEvent == null || buttonEvent.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }

        boolean shouldClick = isPressed()
                && isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY());
        setPressed(false);
        if (shouldClick) {
            onClick();
        }
    }
}
