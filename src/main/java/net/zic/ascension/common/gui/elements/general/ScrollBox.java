package net.zic.ascension.common.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScrollBox extends RenderableElement {
    public boolean useCustomChildAdditionLogic = true;

    private int yOffset;
    private final int scrollRate;

    public ScrollBox(UIFrame frame, int scrollRate) {
        super(frame);
        this.scrollRate = Math.max(1, scrollRate);
        setShouldCull(true);
        addEventListener(EasyEvents.MOUSE_SCROLL_EVENT, this::onMouseScroll);
    }


    @Override
    public void createCullRegion(GuiGraphicsExtractor graphics) {
        // GuiGraphicsExtractor records the current pose with the scissor.
        // EasyGUI's base implementation supplies already-global coordinates,
        // which applies the parent transforms twice in 26.1.
        graphics.enableScissor(0, 0, getWidth(), getHeight());
    }

    private void onMouseScroll(EasyEvent event) {
        if (event.isCanceled() || !(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        if (mouseEvent.getScrollY() == 0.0D) {
            return;
        }

        scroll(mouseEvent.getScrollY() < 0.0D ? -1 : 1);
        event.setCanceled(true);
    }

    public int getMaxYScroll() {
        int bottom = 0;
        for (RenderableElement child : getChildren()) {
            if (!child.isActive()) {
                continue;
            }
            bottom = Math.max(
                    bottom,
                    child.getPositioning().getRawY() + child.getHeight() + yOffset
            );
        }
        return Math.max(0, bottom - getHeight());
    }

    protected void updatePos(RenderableElement element) {
        if (getChildren().isEmpty()) {
            return;
        }

        RenderableElement lastChild = getChildren().getLast();
        int nextX = lastChild.getPositioning().getRawX() + lastChild.getWidth();
        if (nextX + element.getWidth() <= getWidth()) {
            element.getPositioning().setFromRawX(nextX);
            element.getPositioning().setFromRawY(lastChild.getPositioning().getRawY());
            return;
        }

        element.getPositioning().setFromRawX(0);
        element.getPositioning().setFromRawY(
                lastChild.getPositioning().getRawY() + lastChild.getHeight()
        );
    }

    @Override
    public void addChild(RenderableElement element) {
        if (useCustomChildAdditionLogic) {
            updatePos(element);
        }
        super.addChild(element);
        updateVisibility(element);
    }

    protected void updateVisibility(RenderableElement element) {
        int top = element.getPositioning().getY();
        int bottom = top + element.getHeight();
        boolean visible = bottom > 0 && top < getHeight();
        element.setVisible(visible);
        element.setActive(visible);
    }

    protected final int getYOffset() {
        return yOffset;
    }

    protected final void refreshChildVisibility() {
        for (RenderableElement child : getChildren()) {
            updateVisibility(child);
        }
    }

    private void updateChildrenY(int change) {
        for (RenderableElement child : getChildren()) {
            child.getPositioning().setY(child.getPositioning().getY() + change);
            updateVisibility(child);
        }
    }

    public void resetScroll() {
        if (yOffset == 0) {
            refreshChildVisibility();
            return;
        }

        int oldYOffset = yOffset;
        yOffset = 0;
        updateChildrenY(oldYOffset);
    }

    public void scroll(int amount) {
        int change = Math.abs(amount) * scrollRate;
        int oldYOffset = yOffset;
        if (amount < 0) {
            yOffset = Math.min(yOffset + change, getMaxYScroll());
        } else {
            yOffset = Math.max(0, yOffset - change);
        }
        updateChildrenY(oldYOffset - yOffset);
    }
}
