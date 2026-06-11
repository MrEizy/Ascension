package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

public class PathOptionsScrollBox extends ScrollBox {
    public PathOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        setWidth(89);
        setHeight(91);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0, getChildren().size() * 14 - getHeight());
    }

    @Override
    protected void updateVisibility(RenderableElement element) {
        int top = element.getPositioning().getY();
        int bottom = top + element.getHeight();
        boolean visible = bottom > 0 && top < getHeight();
        element.setVisible(visible);
        element.setActive(visible);
    }

    @Override
    protected void updatePos(RenderableElement element) {
        if (getChildren().isEmpty()) {
            return;
        }
        RenderableElement last = getChildren().getLast();
        element.getPositioning().setFromRawY(
                last.getPositioning().getRawY() + last.getHeight() + 2
        );
    }
}
