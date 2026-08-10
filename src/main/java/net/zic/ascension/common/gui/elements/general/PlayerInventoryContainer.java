package net.zic.ascension.common.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;

public class PlayerInventoryContainer extends RenderableElement {
    public PlayerInventoryContainer(UIFrame frame) {
        super(frame);
        setWidth(162);
        setHeight(76);

        generateInventory();
        generateHotBar();

    }
    public void generateHotBar(){
        int offset = 9*3;
        for (int i = 0; i < 9; ++i) {
            RenderableElement element = new RenderableElement(getUiFrame());
            element.getPositioning().setY(5+3*18);
            element.getPositioning().setX(1+i*18);
            element.setWidth(16);
            element.setHeight(16);
            element.setId("slot_index_"+(offset+i));

            addChild(element);

        }
    }
    public void generateInventory(){
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                RenderableElement element = new RenderableElement(getUiFrame());
                element.getPositioning().setX(1+l*18);
                element.getPositioning().setY(1+i*18);
                element.setWidth(16);
                element.setHeight(16);
                element.setId("slot_index_"+(i*9+l));
                addChild(element);
            }
        }
    }
}
