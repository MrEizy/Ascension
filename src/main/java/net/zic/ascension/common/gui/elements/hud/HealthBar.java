package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.Config;
import net.zic.ascension.common.gui.data.ClientAscensionData;

import java.text.DecimalFormat;

public class HealthBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/hp_bar.png"
    );

    private static final ITextureData BAR_TEXTURE = new TextureData(TEXTURE, 101, 9);
    private static final DecimalFormat FORMAT = new DecimalFormat("#.0");

    public HealthBar(UIFrame frame) {
        super(frame);
        setWidth(BAR_TEXTURE.getWidth());
        setHeight(BAR_TEXTURE.getHeight());
    }

    private EasyLabel getOrCreateLabel() {
        if (getChildren().isEmpty()) {
            EasyLabel label = new EasyLabel(getUiFrame());
            addChild(label);

            label.setWidth(75);
            label.setHeight(getHeight());
            label.getPositioning().setXPositioningRule(PositioningRules.CENTER);
            label.getPositioning().setX(-label.getWidth() / 2);
            label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
            label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
            label.setTextColor(0xFFFFFFFF);
            label.setScaleToFit(true);
        }

        return (EasyLabel) getChildren().getFirst();
    }

    private double getMaximumHealth(Player player) {
        double synchronizedMaximum = ClientAscensionData.getAttributeValue(Attributes.MAX_HEALTH);
        if (synchronizedMaximum > 0.0D) {
            return synchronizedMaximum;
        }
        return Math.max(0.0D, player.getMaxHealth());
    }

    private double getDisplayedHealth(Player player, double maximumHealth) {
        if (maximumHealth <= 0.0D) {
            return Math.max(0.0D, player.getHealth());
        }
        return Math.clamp(player.getHealth(), 0.0D, maximumHealth);
    }

    private double getProgress(Player player, double maximumHealth) {
        if (maximumHealth <= 0.0D) {
            return 0.0D;
        }

        return getDisplayedHealth(player, maximumHealth) / maximumHealth;
    }

    private double getAbsorptionProgress(Player player, double maximumHealth) {
        if (maximumHealth <= 0.0D) {
            return 0.0D;
        }

        return Math.clamp(player.getAbsorptionAmount() / maximumHealth, 0.0D, 1.0D);
    }

    private void updateLabel(Player player, double maximumHealth) {
        if (!Config.CLIENT.SHOW_EXACT_HUD_VALUES.get()) {
            clearLabel();
            return;
        }

        EasyLabel label = getOrCreateLabel();
        double displayedHealth = getDisplayedHealth(player, maximumHealth);

        if (getAbsorptionProgress(player, maximumHealth) > 0.0D) {
            label.setText(Component.literal(FORMAT.format(displayedHealth) + "+(" + FORMAT.format(player.getAbsorptionAmount()) + ")/" + FORMAT.format(maximumHealth)));
            return;
        }

        label.setText(Component.literal(FORMAT.format(displayedHealth) + "/" + FORMAT.format(maximumHealth)));
    }

    private void clearLabel() {
        if (!getChildren().isEmpty()) {
            ((EasyLabel) getChildren().getFirst()).setText(Component.empty());
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        double maximumHealth = getMaximumHealth(player);

        updateLabel(player, maximumHealth);

        int width = (int) Math.round(getWidth() * getProgress(player, maximumHealth));

        width = Math.clamp(width, 0, getWidth());

        if (width > 0) {
            BAR_TEXTURE.render(graphics, width, getHeight());
        }
    }
}