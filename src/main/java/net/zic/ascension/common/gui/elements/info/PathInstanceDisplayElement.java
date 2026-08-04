package net.zic.ascension.common.gui.elements.info;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.ScrollBox;

public class PathInstanceDisplayElement extends ScrollBox implements IInformationContainer {
    private static final float DESCRIPTION_SCALE = 0.65F;

    private final EasyLabel realmLabel;
    private final EasyLabel descriptionLabel;

    private int observedParentWidth = -1;
    private int observedParentHeight = -1;
    private boolean sizeFromParent;

    public PathInstanceDisplayElement(UIFrame frame, Component realmName, Component description) {
        this(frame, 0, 0, realmName, description);
        sizeFromParent = true;
    }

    public PathInstanceDisplayElement(
            UIFrame frame,
            int width,
            int height,
            Component realmName,
            Component description
    ) {
        super(frame, 5);
        useCustomChildAdditionLogic = false;
        setWidth(Math.max(0, width));
        setHeight(Math.max(0, height));

        realmLabel = new EasyLabel(frame);
        realmLabel.setText(Component.empty());
        realmLabel.setTextColor(0xFFFFFFFF);
        realmLabel.setHeight(15);
        realmLabel.getPositioning().setX(2);
        realmLabel.getPositioning().setY(5);
        realmLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        realmLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(realmLabel);

        descriptionLabel = new EasyLabel(frame);
        descriptionLabel.setText(Component.empty());
        descriptionLabel.setTextColor(0xFFFFFFFF);
        descriptionLabel.setTextScale(DESCRIPTION_SCALE);
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.getPositioning().setY(24);
        addChild(descriptionLabel);

        applyDimensions();
        setInformation(realmName, description);
    }

    public void setInformation(Component realmName, Component description) {
        resetScroll();

        realmLabel.setText(realmName == null ? Component.empty() : realmName);
        realmLabel.setTextScale(1.0F);

        descriptionLabel.setText(description == null ? Component.empty() : description);
        descriptionLabel.setTextScale(DESCRIPTION_SCALE);

        setVisible(true);
        setActive(true);
        realmLabel.setVisible(true);
        realmLabel.setActive(true);
        descriptionLabel.setVisible(true);
        descriptionLabel.setActive(true);
        refreshChildVisibility();
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
        realmLabel.setWidth(contentWidth);
        descriptionLabel.setWidth(contentWidth);
        descriptionLabel.setFitHeight(true);

        getPositioning().updatePositionMatrix();
        realmLabel.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();
        setVisible(true);
        setActive(true);
        realmLabel.setVisible(true);
        realmLabel.setActive(true);
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
