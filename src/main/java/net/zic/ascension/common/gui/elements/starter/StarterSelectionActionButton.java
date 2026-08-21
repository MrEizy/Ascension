package net.zic.ascension.common.gui.elements.starter;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class StarterSelectionActionButton extends BetterButton {
    public static final int WIDTH = 23;
    public static final int HEIGHT = 24;

    private static final int TEXTURE_HEIGHT = 48;

    private final StarterSelectionContainer owner;
    private final Action action;
    private final ITextureData activeTexture;
    private final ITextureData inactiveTexture;

    public StarterSelectionActionButton(
            UIFrame frame,
            StarterSelectionContainer owner,
            Action action
    ) {
        super(frame, 0, 0);
        this.owner = owner;
        this.action = action;

        Identifier texture = Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                action == Action.CONFIRM ? "textures/gui/main/selection_screens/confirm_button.png" : "textures/gui/main/selection_screens/cancel_button.png"
        );

        this.activeTexture = new TextureDataSubsection(
                texture,
                WIDTH,
                TEXTURE_HEIGHT,
                0,
                0,
                WIDTH,
                HEIGHT
        );
        this.inactiveTexture = new TextureDataSubsection(
                texture,
                WIDTH,
                TEXTURE_HEIGHT,
                0,
                HEIGHT,
                WIDTH,
                HEIGHT
        );

        setWidth(WIDTH);
        setHeight(HEIGHT);
    }

    @Override
    public void onClick() {
        if (!owner.hasSelection()) {
            return;
        }

        if (action == Action.CONFIRM) {
            owner.confirmSelection();
        } else {
            owner.clearSelection();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (owner.hasSelection()) {
            activeTexture.render(graphics);
        } else {
            inactiveTexture.render(graphics);
        }
    }

    public enum Action {
        CANCEL,
        CONFIRM
    }
}
