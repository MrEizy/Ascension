package net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollBox;

public class DescriptionDisplayContainer extends ScrollBox implements IInformationContainer{
    private final EasyLabel titleLabel;
    private final EasyLabel descriptionLabel;
    public DescriptionDisplayContainer(UIFrame uiFrame, Component title, Component description){
        super(uiFrame,9);


        useCustomChildAdditionLogic = false;
        titleLabel = new EasyLabel(getUiFrame());
        titleLabel.setText(title);
        titleLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        titleLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        titleLabel.getPositioning().setY(5);
        titleLabel.getPositioning().setX(2);
        titleLabel.setScaleToFit(true);
        titleLabel.setTextColor(-1);

        addChild(titleLabel);


        descriptionLabel = new EasyLabel(uiFrame);
        descriptionLabel.setText(description == null?Component.empty():description);

        descriptionLabel.getPositioning().setY(15);
        descriptionLabel.getPositioning().setX(2);
        descriptionLabel.setTextColor(-1);
        descriptionLabel.setTextScale(0.8f);
        addChild(descriptionLabel);
        //System.out.println("created container");
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    @Override
    public void refresh() {
        setWidth(getParent().getWidth());
        setHeight(getParent().getHeight());

        titleLabel.setWidth(getWidth()-4);
        descriptionLabel.setWidth(getWidth()-4);
        titleLabel.getPositioning().updatePositionMatrix();
        descriptionLabel.getPositioning().updatePositionMatrix();

    }
}
