package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.client.keybind.ModKeybinds;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.skill_casting.SkillHotBarContainer;
import net.zic.ascension.network.SelectSkillSlotPacket;

public final class SkillWheelOverlay {
    private static final double DEAD_ZONE = 12.0D;

    private static UIFrame frame;
    private static SkillHotBarContainer container;
    private static boolean open;

    private SkillWheelOverlay() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean shouldOpen = minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.player.isSpectator()
                && !minecraft.options.hideGui
                && ModKeybinds.OPEN_SKILL_WHEEL.isDown();

        if (shouldOpen && !open) {
            open();
        } else if (!shouldOpen && open) {
            close();
        }

        if (open) {
            updateSelection();
        }
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (!open) {
            return;
        }

        ensureFrame();
        container.refreshFromHandler();
        frame.run(graphics, getMouseX(), getMouseY(), partialTick);
    }

    private static void open() {
        ensureFrame();
        open = true;
        container.refreshFromHandler();
        container.syncSelectionFromHandler();
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    private static void close() {
        open = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    private static void ensureFrame() {
        if (frame != null) {
            return;
        }

        frame = new UIFrame();
        frame.setPauseGame(false);
        container = new SkillHotBarContainer(frame);
        frame.setRoot(container);
    }

    private static void updateSelection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ClientAscensionData.getSkillCastHandler().ifPresent(handler -> {
            int maxSlots = handler.getMaxSlots();
            if (maxSlots <= 0) {
                return;
            }

            double centerX = minecraft.getWindow().getGuiScaledWidth() / 2.0D;
            double centerY = minecraft.getWindow().getGuiScaledHeight() / 2.0D;
            double dx = getMouseX() - centerX;
            double dy = getMouseY() - centerY;

            if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) {
                container.setSelectedSlot(handler.getSelectedSlot());
                return;
            }

            double segmentDegrees = 360.0D / maxSlots;
            double clockwiseFromTop = (Math.toDegrees(Math.atan2(dy, dx)) + 90.0D + 360.0D) % 360.0D;
            int selected = (int) Math.floor((clockwiseFromTop + segmentDegrees / 2.0D) / segmentDegrees) % maxSlots;

            if (selected == container.getSelectedSlot()) {
                return;
            }

            container.setSelectedSlot(selected);
            ClientPacketDistributor.sendToServer(new SelectSkillSlotPacket(selected));
        });
    }

    private static int getMouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        return (int) Math.round(minecraft.mouseHandler.xpos() / minecraft.getWindow().getGuiScale());
    }

    private static int getMouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        return (int) Math.round(minecraft.mouseHandler.ypos() / minecraft.getWindow().getGuiScale());
    }
}
