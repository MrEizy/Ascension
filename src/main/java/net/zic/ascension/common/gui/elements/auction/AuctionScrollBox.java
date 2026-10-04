package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

final class AuctionScrollBox extends ScrollBox {
    private int contentHeight;

    AuctionScrollBox(UIFrame frame, int width, int height, int scrollRate) {
        super(frame, scrollRate);
        useCustomChildAdditionLogic = false;
        setWidth(width);
        setHeight(height);
    }

    void addRow(RenderableElement row, int y) {
        row.getPositioning().setFromRawX(0);
        row.getPositioning().setFromRawY(y);
        addChild(row);
        contentHeight = Math.max(contentHeight, y + row.getHeight());
        updateVisibility(row);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0, contentHeight - getHeight());
    }
}
