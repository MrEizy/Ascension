package net.zic.ascension.common.gui.elements.info;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

public class DescriptionDisplayContainer extends ScrollBox implements IInformationContainer {
    private final EasyLabel titleLabel;
    private final EasyLabel descriptionLabel;

    public DescriptionDisplayContainer(UIFrame frame, Component title, Component description) {
        super(frame, 9);
        useCustomChildAdditionLogic = false;

        titleLabel = new EasyLabel(frame);
        titleLabel.setText(title == null ? Component.empty() : title);
        titleLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        titleLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        titleLabel.getPositioning().setX(2);
        titleLabel.getPositioning().setY(5);
        titleLabel.setScaleToFit(true);
        titleLabel.setTextColor(0xFFFFFFFF);
        addChild(titleLabel);

        descriptionLabel = new EasyLabel(frame);
        descriptionLabel.setText(description == null ? Component.empty() : description);
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.getPositioning().setY(18);
        descriptionLabel.setTextColor(0xFFFFFFFF);
        descriptionLabel.setTextScale(0.8F);
        addChild(descriptionLabel);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0, descriptionLabel.getHeight() + 24 - getHeight());
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
    public void refresh() {
        if (getParent() == null) {
            return;
        }

        setWidth(getParent().getWidth());
        setHeight(getParent().getHeight());

        int contentWidth = Math.max(1, getWidth() - 4);
        titleLabel.setWidth(contentWidth);
        titleLabel.setHeight(10);
        descriptionLabel.setWidth(contentWidth);
        titleLabel.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();
        updateVisibility(titleLabel);
        updateVisibility(descriptionLabel);
    }
}
