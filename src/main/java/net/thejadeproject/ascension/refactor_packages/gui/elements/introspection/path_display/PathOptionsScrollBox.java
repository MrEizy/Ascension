package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollBox;

public class PathOptionsScrollBox extends ScrollBox {
    public PathOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        setWidth(89);
        setHeight(91);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0,getChildren().size()*12-getHeight());
    }

    @Override
    public void updateVisibility(RenderableElement element) {
        boolean condition =
                !(element.getPositioning().getY()+element.getHeight() <= 0) &&
                        !(element.getPositioning().getY() > getHeight());
        element.setVisible(condition);
    }
    @Override
    public void updatePos(RenderableElement element) {
        if(getChildren().isEmpty()) return;
        element.getPositioning().setFromRawY(getChildren().getLast().getPositioning().getRawY()+getChildren().getLast().getHeight()+2);
    }


}
