package net.zic.ascension.refactor_packages.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;

public class Container extends RenderableElement {
    public Container(UIFrame frame,int width,int height) {
        super(frame);
        setHeight(height);
        setWidth(width);
    }
}
