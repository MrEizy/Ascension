package net.zic.ascension.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.client.keybind.ModKeybinds;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.skill_casting.SkillHotBarContainer;
import net.zic.ascension.network.SelectSkillSlotPacket;

public final class SkillWheelOverlay {
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
                && ModKeybinds.OPEN_SKILL_WHEEL.isDown()
                && !QiCoatingWheelOverlay.wantsOpen();

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

        ensureContainer();
        container.refreshFromHandler();
        RadialWheelLayout.render(graphics, container::render);
    }

    private static void open() {
        ensureContainer();
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

    private static void ensureContainer() {
        if (container == null) {
            container = new SkillHotBarContainer();
        }
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

            int selected = RadialWheelLayout.selectedSlot(minecraft, maxSlots);
            if (selected < 0) {
                container.setSelectedSlot(handler.getSelectedSlot());
                return;
            }

            if (selected == container.getSelectedSlot()) {
                return;
            }

            container.setSelectedSlot(selected);
            //selected locally right away so a cast straight after uses the new slot
            handler.select(selected);
            ClientPacketDistributor.sendToServer(new SelectSkillSlotPacket(selected));
        });
    }
}
