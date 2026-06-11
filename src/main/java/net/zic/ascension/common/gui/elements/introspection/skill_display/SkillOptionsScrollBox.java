package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillOptionsScrollBox extends ScrollBox {
    private final Map<Identifier, SkillSelectionButton> buttons = new HashMap<>();
    private Set<Identifier> displayedSkills = Set.of();
    private int displayedSkillCount;

    public SkillOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        useCustomChildAdditionLogic = false;
        setWidth(89);
        setHeight(91);
    }

    public void setSkills(SkillDisplayContainer owner, List<Identifier> skills) {
        resetScroll();
        displayedSkills = new HashSet<>(skills);
        displayedSkillCount = skills.size();

        for (SkillSelectionButton button : buttons.values()) {
            button.setActive(false);
            button.setVisible(false);
        }

        for (int index = 0; index < skills.size(); index++) {
            Identifier skillId = skills.get(index);
            SkillSelectionButton button = buttons.get(skillId);
            if (button == null) {
                button = new SkillSelectionButton(getUiFrame(), owner, skillId);
                buttons.put(skillId, button);
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
        return Math.max(0, displayedSkillCount * 14 - getHeight());
    }

    @Override
    protected void updateVisibility(RenderableElement element) {
        if (element instanceof SkillSelectionButton button
                && !displayedSkills.contains(button.getSkillId())) {
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
