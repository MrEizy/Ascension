package net.zic.ascension.common.gui.elements.introspection.technique_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TechniqueOptionsScrollBox extends ScrollBox {
    private final Map<Identifier, TechniqueSelectionButton> buttons = new HashMap<>();
    private Set<Identifier> displayedTechniques = Set.of();
    private int displayedTechniqueCount;

    public TechniqueOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        useCustomChildAdditionLogic = false;
        setWidth(89);
        setHeight(91);
    }

    public void setTechniques(TechniqueDisplayContainer owner, List<Identifier> techniques) {
        resetScroll();
        displayedTechniques = new HashSet<>(techniques);
        displayedTechniqueCount = techniques.size();

        for (TechniqueSelectionButton button : buttons.values()) {
            button.setActive(false);
            button.setVisible(false);
        }

        for (int index = 0; index < techniques.size(); index++) {
            Identifier techniqueId = techniques.get(index);
            TechniqueSelectionButton button = buttons.get(techniqueId);
            if (button == null) {
                button = new TechniqueSelectionButton(getUiFrame(), owner, techniqueId);
                buttons.put(techniqueId, button);
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
        return Math.max(0, displayedTechniqueCount * 14 - getHeight());
    }

    @Override
    protected void updateVisibility(RenderableElement element) {
        if (element instanceof TechniqueSelectionButton button
                && !displayedTechniques.contains(button.getTechniqueId())) {
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
