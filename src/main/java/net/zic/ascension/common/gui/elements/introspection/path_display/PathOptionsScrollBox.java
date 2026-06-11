package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathOptionsScrollBox extends ScrollBox {
    private final Map<Identifier, PathSelectionButton> buttons = new HashMap<>();
    private Set<Identifier> displayedPaths = Set.of();
    private int displayedPathCount;

    public PathOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        useCustomChildAdditionLogic = false;
        setWidth(89);
        setHeight(91);
    }

    public void setPaths(PathDisplayContainer owner, List<Identifier> paths) {
        resetScroll();
        displayedPaths = new HashSet<>(paths);
        displayedPathCount = paths.size();

        for (PathSelectionButton button : buttons.values()) {
            button.setActive(false);
            button.setVisible(false);
        }

        for (int index = 0; index < paths.size(); index++) {
            Identifier pathId = paths.get(index);
            PathSelectionButton button = buttons.get(pathId);
            if (button == null) {
                button = new PathSelectionButton(getUiFrame(), owner, pathId);
                buttons.put(pathId, button);
                addChild(button);
            }

            button.refreshTitle();
            button.getPositioning().setFromRawX(0);
            button.getPositioning().setFromRawY(index * 14);
            updateVisibility(button);
        }
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0, displayedPathCount * 14 - getHeight());
    }

    @Override
    protected void updateVisibility(RenderableElement element) {
        if (element instanceof PathSelectionButton button
                && !displayedPaths.contains(button.getPathId())) {
            element.setVisible(false);
            element.setActive(false);
            return;
        }

        int top = element.getPositioning().getY();
        int bottom = top + element.getHeight();
        boolean visible = bottom > 0 && top < getHeight();
        element.setVisible(visible);
        element.setActive(visible);
    }
}
