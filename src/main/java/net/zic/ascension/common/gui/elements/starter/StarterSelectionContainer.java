package net.zic.ascension.common.gui.elements.starter;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.ascension.network.ChooseStarterOptionPacket;

import java.util.List;

public class StarterSelectionContainer extends RenderableElement {
    private static final int TITLE_HEIGHT = 14;
    private static final int TITLE_GAP = 10;

    private static final float BLOODLINE_CARD_SCALE = 0.68F;
    private static final float PHYSIQUE_CARD_SCALE = 0.92F;

    private static final int BLOODLINE_CARD_GAP = 6;
    private static final int PHYSIQUE_CARD_GAP = 10;

    private static final int ACTION_GAP_Y = 10;
    private static final int ACTION_GAP_X = 8;
    private static final int SCREEN_MARGIN_X = 12;
    private static final int SCREEN_MARGIN_Y = 12;

    private final StarterSelectionStage stage;
    private Identifier selectedOption;

    public StarterSelectionContainer(
            UIFrame frame,
            StarterSelectionStage stage,
            List<Identifier> options
    ) {
        super(frame);
        this.stage = stage;

        int optionCount = Math.max(1, options.size());
        int cardGap = gapFor(stage);
        float cardScale = scaleFor(frame, stage, optionCount, cardGap);
        int cardWidth = StarterOptionButton.widthFor(cardScale);
        int cardHeight = StarterOptionButton.heightFor(cardScale);
        int totalCardsWidth = optionCount * cardWidth + Math.max(0, optionCount - 1) * cardGap;

        int actionWidth = StarterSelectionActionButton.WIDTH * 2 + ACTION_GAP_X;
        int actionY = TITLE_HEIGHT + TITLE_GAP + cardHeight + ACTION_GAP_Y;

        setWidth(Math.max(totalCardsWidth, actionWidth));
        setHeight(actionY + StarterSelectionActionButton.HEIGHT);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        EasyLabel titleLabel = new EasyLabel(frame);
        titleLabel.setText(titleFor(stage));
        titleLabel.setTextColor(0xFFFFFFFF);
        titleLabel.setWidth(getWidth());
        titleLabel.setHeight(TITLE_HEIGHT);
        titleLabel.getPositioning().setX(0);
        titleLabel.getPositioning().setY(0);
        titleLabel.setScaleToFit(true);
        titleLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        titleLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(titleLabel);

        int cardsX = (getWidth() - totalCardsWidth) / 2;
        int x = cardsX;
        int y = TITLE_HEIGHT + TITLE_GAP;

        for (Identifier option : options) {
            StarterOptionButton button = new StarterOptionButton(
                    frame,
                    this,
                    stage,
                    option,
                    cardScale
            );

            button.getPositioning().setX(x);
            button.getPositioning().setY(y);
            addChild(button);

            x += cardWidth + cardGap;
        }

        int actionX = (getWidth() - actionWidth) / 2;

        StarterSelectionActionButton cancelButton = new StarterSelectionActionButton(
                frame,
                this,
                StarterSelectionActionButton.Action.CANCEL
        );
        cancelButton.getPositioning().setX(actionX);
        cancelButton.getPositioning().setY(actionY);
        addChild(cancelButton);

        StarterSelectionActionButton confirmButton = new StarterSelectionActionButton(
                frame,
                this,
                StarterSelectionActionButton.Action.CONFIRM
        );
        confirmButton.getPositioning().setX(actionX + StarterSelectionActionButton.WIDTH + ACTION_GAP_X);
        confirmButton.getPositioning().setY(actionY);
        addChild(confirmButton);
    }

    public void selectOption(Identifier optionId) {
        if (optionId != null) {
            selectedOption = optionId;
        }
    }

    public void clearSelection() {
        selectedOption = null;
    }

    public boolean hasSelection() {
        return selectedOption != null;
    }

    public boolean isSelected(Identifier optionId) {
        return selectedOption != null && selectedOption.equals(optionId);
    }

    public void confirmSelection() {
        if (selectedOption == null) {
            return;
        }

        ClientPacketDistributor.sendToServer(new ChooseStarterOptionPacket(stage, selectedOption));
    }

    private static float scaleFor(UIFrame frame, StarterSelectionStage stage, int optionCount, int cardGap) {
        float preferredScale = stage == StarterSelectionStage.PHYSIQUE
                ? PHYSIQUE_CARD_SCALE
                : BLOODLINE_CARD_SCALE;

        int availableWidth = Math.max(1, frame.getWidth() - SCREEN_MARGIN_X * 2);
        int availableHeight = Math.max(1, frame.getHeight() - SCREEN_MARGIN_Y * 2);

        int totalGapWidth = Math.max(0, optionCount - 1) * cardGap;
        float widthScale = Math.max(0.1F,
                (availableWidth - totalGapWidth) / (float) (optionCount * StarterOptionButton.PANEL_WIDTH));

        int fixedHeight = TITLE_HEIGHT + TITLE_GAP + ACTION_GAP_Y + StarterSelectionActionButton.HEIGHT;
        float heightScale = Math.max(0.1F,
                (availableHeight - fixedHeight) / (float) StarterOptionButton.PANEL_HEIGHT);

        return Math.min(preferredScale, Math.min(widthScale, heightScale));
    }

    private static int gapFor(StarterSelectionStage stage) {
        return stage == StarterSelectionStage.PHYSIQUE
                ? PHYSIQUE_CARD_GAP
                : BLOODLINE_CARD_GAP;
    }

    private static Component titleFor(StarterSelectionStage stage) {
        if (stage == StarterSelectionStage.PHYSIQUE) {
            return Component.translatable("gui.ascension.starter.choose_physique");
        }

        return Component.translatable("gui.ascension.starter.choose_bloodline");
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }
}
