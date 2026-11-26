package net.zic.ascension.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

final class RadialWheelLayout {
    private static final double PREFERRED_SCALE = 3.0D;
    private static final double DEAD_ZONE = 12.0D;
    private static final int CENTER_X = 130;
    private static final int CENTER_Y = 110;

    private RadialWheelLayout() {
    }

    private static double wheelScale(int width, int height) {
        return Math.min(PREFERRED_SCALE, Math.min(width / 280.0D, height / 280.0D));
    }

    static void render(GuiGraphicsExtractor graphics, Consumer<GuiGraphicsExtractor> renderer) {
        var window = Minecraft.getInstance().getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        if (width <= 0 || height <= 0 || graphics.guiWidth() <= 0 || graphics.guiHeight() <= 0) {
            return;
        }

        double scale = wheelScale(width, height);
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);
            graphics.pose().scale((float) (scale * graphics.guiWidth() / width),
                    (float) (scale * graphics.guiHeight() / height));
            graphics.pose().translate(-CENTER_X, -CENTER_Y);
            renderer.accept(graphics);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    static int selectedSlot(Minecraft minecraft, int slots) {
        var window = minecraft.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        if (slots <= 0 || width <= 0 || height <= 0 || guiWidth <= 0 || guiHeight <= 0
                || window.getScreenWidth() <= 0 || window.getScreenHeight() <= 0) {
            return -1;
        }

        double scale = wheelScale(width, height);
        double dx = (minecraft.mouseHandler.getScaledXPos(window) - guiWidth / 2.0D)
                * width / guiWidth / scale;
        double dy = (minecraft.mouseHandler.getScaledYPos(window) - guiHeight / 2.0D)
                * height / guiHeight / scale;
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) {
            return -1;
        }

        double segmentDegrees = 360.0D / slots;
        double clockwiseFromTop = (Math.toDegrees(Math.atan2(dy, dx)) + 90.0D + 360.0D) % 360.0D;
        return (int) Math.floor((clockwiseFromTop + segmentDegrees / 2.0D) / segmentDegrees) % slots;
    }
}
