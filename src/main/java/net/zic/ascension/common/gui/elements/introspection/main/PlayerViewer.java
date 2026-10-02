package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import org.joml.Vector2f;

public class PlayerViewer extends RenderableElement {
    private static final float ROTATION_STEP = 22.5F;

    private float rotation;
    private float targetRotation;

    public PlayerViewer(UIFrame frame) {
        super(frame);
        setWidth(54);
        setHeight(77);
    }

    public void rotate(int direction) {
        targetRotation += ROTATION_STEP * direction;
    }

    private void updateRotation() {
        float difference = targetRotation - rotation;
        while (difference > 180.0F) {
            difference -= 360.0F;
        }
        while (difference < -180.0F) {
            difference += 360.0F;
        }
        rotation += difference * 0.22F;
        if (Math.abs(difference) < 0.05F) {
            rotation = targetRotation;
        }
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateRotation();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Vector2f topLeft = getGlobalPoint();
        Vector2f bottomRight = getGlobalCornerPoint();
        ClientAscensionData.getPlayer().ifPresent(player ->
                InventoryScreen.renderEntityInInventoryFollowsAngle(
                        graphics,
                        Math.round(topLeft.x),
                        Math.round(topLeft.y),
                        Math.round(bottomRight.x),
                        Math.round(bottomRight.y),
                        29,
                        0.0F,
                        (float) Math.toRadians(rotation),
                        0.0F,
                        player
                )
        );
    }
}
