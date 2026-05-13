package net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollBox;

public class PathDataDisplayElement extends ScrollBox implements IInformationContainer {
    EasyLabel progress;
    EasyLabel descriptionLabel;
    public PathDataDisplayElement(UIFrame frame, Component majorRealmName,Component minorRealmName,Component description) {
        super(frame, 4);
        useCustomChildAdditionLogic = false;

        progress = new EasyLabel(frame);
        progress.setText(Component.empty().append(majorRealmName).append("(").append(minorRealmName).append(")"));
        progress.setTextColor(-1);
        progress.setScaleToFit(true);
        progress.setHeight(15);
        progress.getPositioning().setY(5);
        progress.getPositioning().setX(2);
        progress.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        addChild(progress);

        descriptionLabel = new EasyLabel(frame);
        descriptionLabel.setText(description == null?Component.empty():description);

        descriptionLabel.getPositioning().setY(25);
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.setTextColor(-1);
        descriptionLabel.setTextScale(0.5f);
        addChild(descriptionLabel);

    }
    @Override
    public int getMaxYScroll() {
        return Math.max(0,descriptionLabel.getHeight()+30 - getHeight());
    }

    @Override
    public void updateVisibility(RenderableElement element) {
        boolean condition =
                !(element.getPositioning().getY()+element.getHeight() <= 0) &&
                        !(element.getPositioning().getY() > getHeight());
        element.setVisible(condition);
    }

    @Override
    public void refresh() {
        setWidth(getParent().getWidth());
        setHeight(getParent().getHeight());

        progress.setWidth(getWidth()-4);
        descriptionLabel.setWidth(getWidth()-4);
        progress.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();
    }
}
