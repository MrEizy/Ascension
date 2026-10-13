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
        int y = height / 2 + 12;
        int row = 0;
        addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.talk"),
                b -> send(NpcActionPacket.Action.TALK)).bounds(x, y + 25 * row++, 192, 20).build());
        if (packet.guidance() && !packet.challenged()) {
            addRenderableWidget(Button.builder(Component.translatable(packet.enlightened()
                    ? "gui.ascension.npc.enlightened" : "gui.ascension.npc.guidance"),
                    b -> send(NpcActionPacket.Action.GUIDANCE)).bounds(x, y + 25 * row++, 192, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable(packet.challenged()
                ? "gui.ascension.npc.withdraw" : "gui.ascension.npc.challenge"), b -> {
            send(packet.challenged() ? NpcActionPacket.Action.WITHDRAW : NpcActionPacket.Action.CHALLENGE);
            onClose();
        }).bounds(x, y + 25 * row++, 192, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.leave"),
                b -> onClose()).bounds(x, y + 25 * row, 192, 20).build());
    }

    private void send(NpcActionPacket.Action action) {
        ClientPacketDistributor.sendToServer(new NpcActionPacket(packet.entityId(), packet.uuid(), action));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int left = width / 2 - 125;
        int top = height / 2 - 88;
        graphics.fill(left - 6, top - 9, left + 256, top + 83, 0xCC15171C);
        graphics.text(font, packet.name(), left, top, 0xFFFFD899, true);
        var lines = font.split(Component.literal(packet.message()), 242);
        for (int i = 0; i < Math.min(5, lines.size()); i++) {
            graphics.text(font, lines.get(i), left, top + 21 + i * 11, 0xFFFFFFFF, false);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}
