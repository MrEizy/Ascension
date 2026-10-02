package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class PlayerRotationButton extends BetterButton {
    public enum Direction {
        LEFT(1, 216),
        RIGHT(-1, 203);

        private final int rotationDirection;
        private final int textureY;

        Direction(int rotationDirection, int textureY) {
            this.rotationDirection = rotationDirection;
            this.textureY = textureY;
        }
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    private final PlayerViewer viewer;
    private final Direction direction;
    private final ITextureData hoverTexture;

    public PlayerRotationButton(UIFrame frame, PlayerViewer viewer, Direction direction) {
        super(frame, 0, 0);
        this.viewer = viewer;
        this.direction = direction;
        this.hoverTexture = new TextureDataSubsection(
                TEXTURE, 256, 260, 30, direction.textureY, 18, 10
        );
        setWidth(18);
        setHeight(10);
    }

    @Override
    public void onClick() {
        viewer.rotate(direction.rotationDirection);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() || isPressed()) {
            hoverTexture.render(graphics);
        }
    }
}
