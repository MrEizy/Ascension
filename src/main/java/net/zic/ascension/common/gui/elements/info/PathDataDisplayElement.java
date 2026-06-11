package net.zic.ascension.common.gui.elements.info;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

public class PathDataDisplayElement extends ScrollBox implements IInformationContainer {
    private final EasyLabel realmLabel;
    private final EasyLabel descriptionLabel;

    public PathDataDisplayElement(UIFrame frame, Component realmName, Component description) {
        super(frame, 5);
        useCustomChildAdditionLogic = false;

        realmLabel = new EasyLabel(frame);
        realmLabel.setText(realmName == null ? Component.empty() : realmName);
        realmLabel.setTextColor(0xFFFFFFFF);
        realmLabel.setScaleToFit(true);
        realmLabel.setHeight(15);
        realmLabel.getPositioning().setX(2);
        realmLabel.getPositioning().setY(5);
        realmLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        realmLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(realmLabel);

        descriptionLabel = new EasyLabel(frame);
        descriptionLabel.setText(description == null ? Component.empty() : description);
        descriptionLabel.setTextColor(0xFFFFFFFF);
        descriptionLabel.setTextScale(0.65F);
        descriptionLabel.setFitHeight(true);
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.getPositioning().setY(24);
        addChild(descriptionLabel);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0, descriptionLabel.getHeight() + 30 - getHeight());
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
        setVisible(true);
        setActive(true);

        int contentWidth = Math.max(1, getWidth() - 4);
        realmLabel.setWidth(contentWidth);
        descriptionLabel.setWidth(contentWidth);
        descriptionLabel.setFitHeight(true);

        getPositioning().updatePositionMatrix();
        realmLabel.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();
        updateVisibility(realmLabel);
        updateVisibility(descriptionLabel);
    }
}
