package net.zic.ascension.common.gui.elements.general;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Vector2f;

/**
 * Vertical scroll bar for a {@link ScrollBox}. The element itself is the track,
 * the thumb is drawn inside it and can be dragged or jumped to by clicking the track.
 */
public class ScrollBar extends RenderableElement {
    private static final int MIN_THUMB_HEIGHT = 8;
    private static final int THUMB_FILL = 0xFF1C4F78;
    private static final int THUMB_BORDER = 0xFF3FB4F0;
    private static final int THUMB_BORDER_ACTIVE = 0xFFA5E4FF;

    private final ScrollBox target;
    private boolean dragging;
    private float grabOffset;

    public ScrollBar(UIFrame frame, ScrollBox target, int width, int height) {
        super(frame);
        this.target = target;
        setWidth(width);
        setHeight(height);

        //drag starts from a targeted event so a hidden tab's bar never reacts
        addEventListener(EasyEvents.MOUSE_DOWN_EVENT, this::onMouseDown);
        addEventListener(EasyEvents.MOUSE_SCROLL_EVENT, this::onMouseScroll);
        addEventListener(EasyEvents.GLOBAL_MOUSE_DRAG_EVENT, this::onMouseDrag);
        addEventListener(EasyEvents.GLOBAL_MOUSE_UP_EVENT, this::onMouseUp);
    }

    private void onMouseDown(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent) || !isLeftButton(mouseEvent)) {
            return;
        }
        int maximum = target.getMaxYScroll();
        if (maximum <= 0 || !isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY())) {
            return;
        }

        float localY = toLocalY(mouseEvent);
        int thumbHeight = thumbHeight(maximum);
        int thumbTop = thumbTop(maximum, thumbHeight);
        if (localY >= thumbTop && localY < thumbTop + thumbHeight) {
            grabOffset = localY - thumbTop;
        } else {
            grabOffset = thumbHeight / 2.0F;
            moveThumbTo(localY - grabOffset, maximum, thumbHeight);
        }
        dragging = true;
    }

    private void onMouseDrag(EasyEvent event) {
        if (!dragging || !(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        int maximum = target.getMaxYScroll();
        if (maximum <= 0) {
            dragging = false;
            return;
        }
        moveThumbTo(toLocalY(mouseEvent) - grabOffset, maximum, thumbHeight(maximum));
    }

    private void onMouseUp(EasyEvent event) {
        if (event instanceof EasyMouseEvent mouseEvent && isLeftButton(mouseEvent)) {
            dragging = false;
        }
    }

    private void onMouseScroll(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent) || mouseEvent.getScrollY() == 0.0D) {
            return;
        }
        target.scroll(mouseEvent.getScrollY() < 0.0D ? -1 : 1);
    }

    private void moveThumbTo(float thumbTop, int maximum, int thumbHeight) {
        int travel = getHeight() - thumbHeight;
        if (travel <= 0) {
            return;
        }
        float progress = Math.clamp(thumbTop / travel, 0.0F, 1.0F);
        target.setScrollOffset(Math.round(progress * maximum));
    }

    private int thumbHeight(int maximum) {
        int viewHeight = Math.max(1, target.getHeight());
        int height = Math.round((float) getHeight() * viewHeight / (viewHeight + maximum));
        return Math.clamp(height, Math.min(MIN_THUMB_HEIGHT, getHeight()), getHeight());
    }

    private int thumbTop(int maximum, int thumbHeight) {
        int offset = Math.clamp(target.getYOffset(), 0, maximum);
        return Math.round((float) (getHeight() - thumbHeight) * offset / maximum);
    }

    private float toLocalY(EasyMouseEvent mouseEvent) {
        Vector2f local = globalToLocalPoint((float) mouseEvent.getMouseX(), (float) mouseEvent.getMouseY());
        return local.y;
    }

    private static boolean isLeftButton(EasyMouseEvent mouseEvent) {
        MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
        return buttonEvent != null && buttonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int maximum = target.getMaxYScroll();
        if (maximum <= 0) {
            return;
        }

        int thumbHeight = thumbHeight(maximum);
        int thumbTop = thumbTop(maximum, thumbHeight);
        int border = dragging || isPointBounded(mouseX, mouseY) ? THUMB_BORDER_ACTIVE : THUMB_BORDER;
        graphics.fill(0, thumbTop, getWidth(), thumbTop + thumbHeight, border);
        graphics.fill(1, thumbTop + 1, getWidth() - 1, thumbTop + thumbHeight - 1, THUMB_FILL);
    }
}
