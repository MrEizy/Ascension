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
    private final boolean confirmingMortal;

    private NpcDialogueScreen(NpcDialoguePacket packet) { this(packet, false); }

    private NpcDialogueScreen(NpcDialoguePacket packet, boolean confirmingMortal) {
        super(Component.literal(packet.name()));
        this.packet = packet;
        this.confirmingMortal = confirmingMortal;
    }

    public static void open(NpcDialoguePacket packet) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet));
        }
    }

    @Override
    protected void init() {
        int x = width / 2 - 96;
        int y = height / 2 + (confirmingMortal ? 18 : 3);
        int row = 0;
        if (confirmingMortal) {
            addRenderableWidget(Button.builder(Component.literal("Confirm Fight to the Death"), b -> {
                send(NpcActionPacket.Action.MORTAL_CONFRONTATION);
                onClose();
            }).bounds(x, y + 25 * row++, 192, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"),
                    b -> Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet)))
                    .bounds(x, y + 25 * row, 192, 20).build());
            return;
        }
        if (!packet.challenged()) addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.talk"),
                b -> send(NpcActionPacket.Action.TALK)).bounds(x, y + 25 * row++, 192, 20).build());
        if (packet.guidance() && packet.guidanceUnlocked() && !packet.challenged()) {
            addRenderableWidget(Button.builder(Component.translatable(packet.enlightened()
                    ? "gui.ascension.npc.enlightened" : "gui.ascension.npc.guidance"),
                    b -> send(NpcActionPacket.Action.GUIDANCE)).bounds(x, y + 25 * row++, 192, 20).build());
        }
        if (!packet.challenged()) {
            if (packet.giftUnlocked()) addRenderableWidget(Button.builder(Component.literal("Offer Gift"),
                    b -> send(NpcActionPacket.Action.GIFT)).bounds(x, y + 25 * row++, 192, 20).build());
            if (packet.insultUnlocked()) addRenderableWidget(Button.builder(Component.literal("Insult"),
                    b -> send(NpcActionPacket.Action.INSULT)).bounds(x, y + 25 * row++, 192, 20).build());
            if (packet.challengeUnlocked()) addRenderableWidget(Button.builder(Component.literal(packet.mortalAvailable()
                    ? "Mortal Confrontation" : "Challenge to Duel"), b -> {
                if (packet.mortalAvailable()) Minecraft.getInstance().setScreen(new NpcDialogueScreen(packet, true));
                else {
                    send(NpcActionPacket.Action.CHALLENGE);
                    onClose();
                }
            }).bounds(x, y + 25 * row++, 192, 20).build());
        } else if (!packet.mortalActive()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.ascension.npc.withdraw"), b -> {
                send(NpcActionPacket.Action.WITHDRAW);
                onClose();
            }).bounds(x, y + 25 * row++, 192, 20).build());
        }
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
        int top = height / 2 - 116;
        graphics.fill(left - 6, top - 9, left + 256, height / 2 + 178, 0xCC15171C);
        graphics.text(font, packet.name(), left, top, 0xFFFFD899, true);
        graphics.text(font, "Personality: " + packet.personality(), left, top + 13, 0xFFB5C5D0, false);
        graphics.text(font, "Standing: " + packet.standing() + " (" + packet.respect() + ")", left, top + 24, 0xFFB5C5D0, false);
        graphics.text(font, packet.affiliation() + " reputation: " + packet.reputation(), left, top + 35, 0xFFB5C5D0, false);
        var lines = font.split(Component.literal(packet.message()), 242);
        for (int i = 0; i < Math.min(5, lines.size()); i++) {
            graphics.text(font, lines.get(i), left, top + 50 + i * 11, 0xFFFFFFFF, false);
        }
        graphics.text(font, "Conversations: " + packet.talks() + " | Duels: " + packet.wins() + "W / " + packet.losses() + "L",
                left, top + 111, 0xFFB5C5D0, false);
        if (confirmingMortal) {
            var warning = font.split(Component.literal("WARNING: This is a fight to the death. Death and loot are possible."), 242);
            for (int i = 0; i < Math.min(2, warning.size()); i++)
                graphics.text(font, warning.get(i), left, top + 116 + i * 11, 0xFFFF7777, false);
        }
        var hint = font.split(Component.literal(packet.giftUnlocked() ? packet.giftHint() :
                "Talk more to get acquainted and unlock interactions."), 242);
        if (!confirmingMortal) for (int i = 0; i < Math.min(2, hint.size()); i++) {
            graphics.text(font, hint.get(i), left, height / 2 + 153 + i * 11, 0xFFC3B6A0, false);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}
