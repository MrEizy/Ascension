package net.thejadeproject.ascension.refactor_packages.gui.elements.general;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;

public class ScrollItemContainer extends ScrollBox {
    private final int totalRows;
    private final int maxVisibleRows;

    @Override
    public void addChild(RenderableElement element) {
        super.addChild(element);

    }

    public ScrollItemContainer(UIFrame frame, int maxVisible, int startSlot, int finalSlot) {
        super(frame,18);//1 row per scroll
        setWidth(18*9);
        this.maxVisibleRows = maxVisible;
        totalRows = Math.ceilDiv(finalSlot-startSlot,9);
        generateSlots(startSlot,finalSlot);
    }

    @Override
    public int getHeight() {
        if(totalRows < maxVisibleRows) return totalRows*18;
        return maxVisibleRows*18;
    }

    @Override
    public int getMaxYScroll() {
        if(totalRows < maxVisibleRows) return 0;
        return (totalRows-maxVisibleRows)*18;
    }

    public void generateSlots(int startSlot, int finalSlot){
        for (int i = 0; i < totalRows; ++i) {
            for (int l = 0; l < 9; ++l) {
                if(finalSlot<(startSlot+i*9+l)) return;
                RenderableElement element = new RenderableElement(getUiFrame());
                addChild(element);
                element.getPositioning().setX(1+l*18);
                element.getPositioning().setY(1+i*18);
                element.setWidth(16);
                element.setHeight(16);
                element.setId("slot_index_"+(startSlot+i*9+l));
                updateVisibility(element);
            }
        }
    }

    @Override
    public void updateVisibility(RenderableElement element) {
        element.setActive(element.getPositioning().getY() >= 1 && element.getPositioning().getY() < getHeight());
    }
}
