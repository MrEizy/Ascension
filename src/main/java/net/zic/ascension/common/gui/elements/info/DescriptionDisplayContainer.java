package net.zic.ascension.common.gui.elements.info;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

public class DescriptionDisplayContainer extends ScrollBox implements IInformationContainer {
    private static final float DESCRIPTION_SCALE = 1F;

    private final EasyLabel titleLabel;
    private final EasyLabel descriptionLabel;

    private int observedParentWidth = -1;
    private int observedParentHeight = -1;
    private boolean sizeFromParent;

    public DescriptionDisplayContainer(UIFrame frame, Component title, Component description) {
        this(frame, 0, 0, title, description);
        sizeFromParent = true;
    }

    public DescriptionDisplayContainer(
            UIFrame frame,
            int width,
            int height,
            Component title,
            Component description
    ) {
        super(frame, 9);
        useCustomChildAdditionLogic = false;
        setWidth(Math.max(0, width));
        setHeight(Math.max(0, height));

        titleLabel = new EasyLabel(frame);
        titleLabel.setText(Component.empty());
        titleLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        titleLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        titleLabel.getPositioning().setX(2);
        titleLabel.getPositioning().setY(5);
        titleLabel.setTextColor(0xFFFFFFFF);
        addChild(titleLabel);

        descriptionLabel = new EasyLabel(frame);
        descriptionLabel.setText(Component.empty());
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.getPositioning().setY(18);
        descriptionLabel.setTextColor(0xFFFFFFFF);
        descriptionLabel.setTextScale(DESCRIPTION_SCALE);
        addChild(descriptionLabel);

        applyDimensions();
        setInformation(title, description);
    }

    public void setInformation(Component title, Component description) {
        resetScroll();

        titleLabel.setText(title == null ? Component.empty() : title);
        titleLabel.setTextScale(1.0F);

        descriptionLabel.setText(description == null ? Component.empty() : description);
        descriptionLabel.setTextScale(DESCRIPTION_SCALE);

        setVisible(true);
        setActive(true);
        titleLabel.setVisible(true);
        titleLabel.setActive(true);
        descriptionLabel.setVisible(true);
        descriptionLabel.setActive(true);
        refreshChildVisibility();
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
        if (sizeFromParent && getParent() != null) {
            observedParentWidth = getParent().getWidth();
            observedParentHeight = getParent().getHeight();
            setWidth(observedParentWidth);
            setHeight(observedParentHeight);
        }
        applyDimensions();
    }

    private void applyDimensions() {
        int contentWidth = Math.max(1, getWidth() - 4);
        titleLabel.setWidth(contentWidth);
        titleLabel.setHeight(10);
        descriptionLabel.setWidth(contentWidth);
        descriptionLabel.setFitHeight(true);

        getPositioning().updatePositionMatrix();
        titleLabel.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();
        setVisible(true);
        setActive(true);
        titleLabel.setVisible(true);
        titleLabel.setActive(true);
        descriptionLabel.setVisible(true);
        descriptionLabel.setActive(true);
        refreshChildVisibility();
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (!sizeFromParent || getParent() == null) {
            return;
        }
        if (getParent().getWidth() != observedParentWidth
                || getParent().getHeight() != observedParentHeight) {
            refresh();
        }
    }
}
