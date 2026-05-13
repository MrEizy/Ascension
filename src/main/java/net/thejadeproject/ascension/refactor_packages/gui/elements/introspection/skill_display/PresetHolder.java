package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;

public class PresetHolder extends RenderableElement {
    private final int presets = 1;
    private int activePreset = 0;
    public PresetHolder(UIFrame frame) {
        super(frame);
    }

    public int getActivePreset(){return activePreset;}
}
