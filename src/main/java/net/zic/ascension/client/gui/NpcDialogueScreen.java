package net.zic.ascension.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.network.npc.NpcActionPacket;
import net.zic.ascension.network.npc.NpcDialoguePacket;

public class NpcDialogueScreen extends Screen {
    private final NpcDialoguePacket packet;

    private NpcDialogueScreen(NpcDialoguePacket packet) {
        super(Component.literal(packet.name()));
        this.packet = packet;
    }

    public static void open(NpcDialoguePacket packet) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet));
        }
    }

    @Override
    protected void init() {
        int x = width / 2 - 96;
        int y = height / 2 + 16;
        addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.talk"),
                b -> send(NpcActionPacket.Action.TALK)).bounds(x, y, 192, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.challenge"), b -> {
            send(NpcActionPacket.Action.CHALLENGE);
            onClose();
        }).bounds(x, y + 25, 192, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.leave"),
                b -> onClose()).bounds(x, y + 50, 192, 20).build());
    }

    private void send(NpcActionPacket.Action action) {
        ClientPacketDistributor.sendToServer(new NpcActionPacket(packet.entityId(), packet.uuid(), action));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int left = width / 2 - 125;
        int top = height / 2 - 70;
        graphics.fill(left - 6, top - 9, left + 256, top + 77, 0xCC15171C);
        graphics.text(font, packet.name(), left, top, 0xFFFFD899, true);
        var lines = font.split(Component.literal(packet.message()), 242);
        for (int i = 0; i < Math.min(4, lines.size()); i++) {
            graphics.text(font, lines.get(i), left, top + 21 + i * 11, 0xFFFFFFFF, false);
        }
    }
}
