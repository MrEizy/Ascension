package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.qi.EntityQi;

import java.text.DecimalFormat;

public class QiBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/qi_bar.png"
    );

    private static final ITextureData BAR_TEXTURE = new TextureData(TEXTURE, 85, 9);
    private static final DecimalFormat FORMAT = new DecimalFormat("#.0");

    public QiBar(UIFrame frame) {
        super(frame);
        setWidth(BAR_TEXTURE.getWidth());
        setHeight(BAR_TEXTURE.getHeight());
    }

    private EasyLabel getOrCreateLabel() {
        if (getChildren().isEmpty()) {
            EasyLabel label = new EasyLabel(getUiFrame());
            addChild(label);

            label.setWidth(60);
            label.setHeight(getHeight());

            label.getPositioning().setXPositioningRule(PositioningRules.CENTER);
            label.getPositioning().setX(-label.getWidth() / 2);

            label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
            label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
            label.setTextColor(-1);
            label.setScaleToFit(true);
        }

        return (EasyLabel) getChildren().getFirst();
    }

    private EntityQi getQi() {
        return ClientAscensionData.getQi().orElse(null);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        EntityQi qi = getQi();

        double currentQi = qi == null ? 0.0D : qi.getCurrentQi();
        double maxQi = qi == null ? 0.0D : qi.getMaxQi();
        double progress = qi == null ? 0.0D : qi.getProgress();

        getOrCreateLabel().setText(Component.literal(
                FORMAT.format(currentQi) + "/" + FORMAT.format(maxQi)
        ));

        int width = (int) Math.round(getWidth() * progress);
        if (width > 0) {
            BAR_TEXTURE.render(graphics, width, getHeight());
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}