package net.zic.ascension.common.gui.elements.starter;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.starter.StarterSelectionStage;

import java.util.List;

public class StarterSelectionContainer extends RenderableElement {
    private static final int TITLE_HEIGHT = 14;
    private static final int TITLE_GAP = 10;

    private static final float BLOODLINE_CARD_SCALE = 0.68F;
    private static final float PHYSIQUE_CARD_SCALE = 0.92F;

    private static final int BLOODLINE_CARD_GAP = 6;
    private static final int PHYSIQUE_CARD_GAP = 10;

    public StarterSelectionContainer(
            UIFrame frame,
            StarterSelectionStage stage,
            List<Identifier> options,
            Identifier selectedBloodline
    ) {
        super(frame);

        int optionCount = Math.max(1, options.size());
        float cardScale = scaleFor(stage);
        int cardGap = gapFor(stage);
        int cardWidth = StarterOptionButton.widthFor(cardScale);
        int cardHeight = StarterOptionButton.heightFor(cardScale);
        int totalCardsWidth = optionCount * cardWidth + Math.max(0, optionCount - 1) * cardGap;

        setWidth(totalCardsWidth);
        setHeight(TITLE_HEIGHT + TITLE_GAP + cardHeight);
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

        int x = 0;
        int y = TITLE_HEIGHT + TITLE_GAP;

        for (Identifier option : options) {
            StarterOptionButton button = new StarterOptionButton(
                    frame,
                    stage,
                    option,
                    selectedBloodline,
                    cardScale
            );

            button.getPositioning().setX(x);
            button.getPositioning().setY(y);
            addChild(button);

            x += cardWidth + cardGap;
        }
    }

    private static float scaleFor(StarterSelectionStage stage) {
        return stage == StarterSelectionStage.PHYSIQUE
                ? PHYSIQUE_CARD_SCALE
                : BLOODLINE_CARD_SCALE;
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