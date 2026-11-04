package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.network.npc.NpcActionPacket;
import net.zic.ascension.network.npc.NpcDialoguePacket;

public final class NpcDialogueScreen extends EasyScreen {
    private final NpcDialoguePacket packet;

    private NpcDialogueScreen(NpcDialoguePacket packet, boolean confirmingMortal) {
        super(Component.literal(packet.name()));
        this.packet = packet;
        UIFrame frame = getUIFrame();
        frame.setPauseGame(false);
        frame.setRoot(new NpcDialogueContainer(frame, this, packet, confirmingMortal));
    }

    public static void open(NpcDialoguePacket packet) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet, false));
        }
    }


    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        minecraft.gui.extractDeferredSubtitles();
    }

    void send(NpcActionPacket.Action action) {
        ClientPacketDistributor.sendToServer(new NpcActionPacket(packet.entityId(), packet.uuid(), action));
    }

    void confirmMortal() {
        Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet, true));
    }

    void cancelMortal() {
        Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet, false));
    }
}
