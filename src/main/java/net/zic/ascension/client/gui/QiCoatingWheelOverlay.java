package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingElement;
import net.zic.ascension.client.keybind.ModKeybinds;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.skill_casting.QiCoatingWheelContainer;
import net.zic.ascension.impl.core.skill.castable.QiCoatingSelectionService;
import net.zic.ascension.network.SelectQiCoatingElementPacket;

import java.util.List;
import java.util.Optional;

/**
 * The coating wheel: Shift + the skill wheel key while a Qi Coating skill is the selected hotbar skill. It lists the elements
 * the player has the path for, by name; the one under the mouse when the wheel closes is picked. Picking only on close keeps
 * a switch (old coating shrinking away, new one growing in) from firing for every element the mouse passes over.
 */
public final class QiCoatingWheelOverlay {
    private static final double DEAD_ZONE = 12.0D;

    private static UIFrame frame;
    private static QiCoatingWheelContainer container;
    private static boolean open;
    private static QiCoatingChannel channel;
    private static List<Identifier> elements = List.of();
    private static int hovered = -1;

    private QiCoatingWheelOverlay() {
    }

    /**
     * Whether the coating wheel should be open right now; the skill wheel stays shut while it is.
     */
    public static boolean wantsOpen() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.player.isSpectator()
                && !minecraft.options.hideGui
                && ModKeybinds.OPEN_SKILL_WHEEL.isDown()
                && minecraft.options.keyShift.isDown()
                && selectedChannel().isPresent();
    }

    public static void onClientTick() {
        boolean shouldOpen = wantsOpen();
        if (shouldOpen && !open) {
            open(selectedChannel().orElseThrow());
        } else if (!shouldOpen && open) {
            close();
        }

        if (open) {
            updateHover();
        }
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (!open) {
            return;
        }
        ensureFrame();
        frame.run(graphics, getMouseX(), getMouseY(), partialTick);
    }

    private static Optional<QiCoatingChannel> selectedChannel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return Optional.empty();
        }
        return ClientAscensionData.getSkillCastHandler()
                .map(handler -> handler.getSkill(handler.getSelectedSlot()))
                .flatMap(skill -> QiCoatingSelectionService.channelOfCastable(skill, minecraft.level.registryAccess()));
    }

    private static void open(QiCoatingChannel openedChannel) {
        Minecraft minecraft = Minecraft.getInstance();
        ensureFrame();
        open = true;
        channel = openedChannel;
        ClientLevel level = minecraft.level;
        elements = level == null ? List.of() : ClientAscensionData.getSource()
                .map(source -> QiCoatingSelectionService.available(source, channel, level.registryAccess()))
                .orElse(List.of());
        container.setNames(elements.stream().map(QiCoatingWheelOverlay::name).toList());
        hovered = stored().map(elements::indexOf).orElse(-1);
        container.setHovered(hovered);
        minecraft.mouseHandler.releaseMouse();
    }

    private static void close() {
        open = false;
        if (hovered >= 0 && hovered < elements.size()) {
            Identifier picked = elements.get(hovered);
            if (!stored().map(picked::equals).orElse(false)) {
                ClientPacketDistributor.sendToServer(new SelectQiCoatingElementPacket(channel, picked));
            }
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    private static Optional<Identifier> stored() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? Optional.empty() : minecraft.player.getData(CoreAttachments.QI_COATING_SELECTION).get(channel);
    }

    private static Component name(Identifier element) {
        Minecraft minecraft = Minecraft.getInstance();
        QiCoatingElement definition = minecraft.level == null ? null
                : CoreRegistries.safeAccess(CoreRegistries.QI_COATING_ELEMENT_REGISTRY, element, minecraft.level.registryAccess());
        return definition == null ? Component.literal(element.getPath()) : definition.name();
    }

    private static void ensureFrame() {
        if (frame != null) {
            return;
        }
        frame = new UIFrame();
        frame.setPauseGame(false);
        container = new QiCoatingWheelContainer(frame);
        frame.setRoot(container);
    }

    private static void updateHover() {
        if (elements.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        double centerX = minecraft.getWindow().getGuiScaledWidth() / 2.0D;
        double centerY = minecraft.getWindow().getGuiScaledHeight() / 2.0D;
        double dx = getMouseX() - centerX;
        double dy = getMouseY() - centerY;
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) {
            return;
        }
        double segmentDegrees = 360.0D / elements.size();
        double clockwiseFromTop = (Math.toDegrees(Math.atan2(dy, dx)) + 90.0D + 360.0D) % 360.0D;
        hovered = (int) Math.floor((clockwiseFromTop + segmentDegrees / 2.0D) / segmentDegrees) % elements.size();
        container.setHovered(hovered);
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
