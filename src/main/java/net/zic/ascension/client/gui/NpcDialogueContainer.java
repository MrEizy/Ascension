package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.zic.ascension.network.npc.NpcActionPacket;
import net.zic.ascension.network.npc.NpcDialoguePacket;

import java.util.ArrayList;
import java.util.List;

public final class NpcDialogueContainer extends RenderableElement {
    private final NpcDialogueScreen screen;
    private final NpcDialoguePacket packet;
    private final boolean confirmingMortal;
    private final List<FormattedCharSequence> messageLines;
    private final boolean wide;
    private int messagePage;

    public NpcDialogueContainer(UIFrame frame, NpcDialogueScreen screen, NpcDialoguePacket packet, boolean confirmingMortal) {
        super(frame);
        this.screen = screen;
        this.packet = packet;
        this.confirmingMortal = confirmingMortal;
        this.wide = frame.getWidth() >= NpcDialogueStyle.WIDE_SCREEN_MIN_WIDTH;
        this.messageLines = Minecraft.getInstance().font.split(Component.literal(confirmingMortal
                ? "WARNING: This confrontation is lethal. Death, loot, and faction consequences are possible."
                : packet.message()), NpcDialogueStyle.PANEL_TEXT_WIDTH);

        List<Choice> choices = buildChoices();
        if (messageLines.size() > NpcDialogueStyle.MESSAGE_LINES_PER_PAGE) {
            choices.add(new Choice(Component.literal("Next dialogue page"), this::nextPage, false));
        }

        setWidth(wide ? NpcDialogueStyle.WIDE_CHOICE_X + NpcDialogueStyle.BUTTON_WIDTH : NpcDialogueStyle.PANEL_WIDTH);
        int rows = wide ? choices.size() : (choices.size() + 1) / 2;
        int wideChoicesHeight = choices.isEmpty() ? 0
                : (choices.size() - 1) * NpcDialogueStyle.WIDE_CHOICE_STEP + NpcDialogueStyle.BUTTON_HEIGHT;
        int wideChoicesY = Math.max(0, NpcDialogueStyle.WIDE_CHOICE_Y
                + (NpcDialogueStyle.PANEL_HEIGHT - wideChoicesHeight) / 2);
        int actionsBottom = wide ? wideChoicesY + wideChoicesHeight
                : NpcDialogueStyle.COMPACT_CHOICE_Y + (rows - 1) * NpcDialogueStyle.COMPACT_CHOICE_STEP_Y
                + NpcDialogueStyle.BUTTON_HEIGHT;
        setHeight(Math.max(NpcDialogueStyle.PANEL_Y + NpcDialogueStyle.PANEL_HEIGHT, actionsBottom));
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-NpcDialogueStyle.PANEL_WIDTH / 2);
        getPositioning().setY(frame.getHeight() / 2 - getHeight() - NpcDialogueStyle.BOTTOM_MARGIN);

        for (int i = 0; i < choices.size(); i++) {
            Choice choice = choices.get(i);
            NpcDialogueChoiceButton button = new NpcDialogueChoiceButton(frame, choice.label(), choice.action(),
                    wide ? 1.0F : NpcDialogueStyle.COMPACT_BUTTON_SCALE, true, choice.dangerous(), choice.tooltip());
            int x = wide ? NpcDialogueStyle.WIDE_CHOICE_X : (i % 2) * NpcDialogueStyle.COMPACT_CHOICE_STEP_X;
            int y = wide ? wideChoicesY + i * NpcDialogueStyle.WIDE_CHOICE_STEP
                    : NpcDialogueStyle.COMPACT_CHOICE_Y + (i / 2) * NpcDialogueStyle.COMPACT_CHOICE_STEP_Y;
            button.getPositioning().setX(x);
            button.getPositioning().setY(y);
            addChild(button);
        }
    }

    private List<Choice> buildChoices() {
        List<Choice> choices = new ArrayList<>();
        if (confirmingMortal) {
            choices.add(new Choice(Component.literal("Confirm mortal combat"), () -> {
                screen.send(NpcActionPacket.Action.MORTAL_CONFRONTATION);
                screen.onClose();
            }, true));
            choices.add(new Choice(Component.literal("Return to dialogue"), screen::cancelMortal, false));
            choices.add(new Choice(Component.translatable("gui.ascension.npc.leave"), screen::onClose, false));
            return choices;
        }
        if (!packet.challenged()) {
            choices.add(new Choice(Component.translatable("gui.ascension.npc.talk"), () -> screen.send(NpcActionPacket.Action.TALK), false));
            if (packet.guidance() && packet.guidanceUnlocked()) {
                choices.add(new Choice(Component.translatable(packet.enlightened()
                        ? "gui.ascension.npc.enlightened" : "gui.ascension.npc.guidance"),
                        () -> screen.send(NpcActionPacket.Action.GUIDANCE), false));
            }
            if (packet.giftUnlocked()) choices.add(new Choice(Component.literal("Offer Gift"), () -> screen.send(NpcActionPacket.Action.GIFT), false, packet.giftHint()));
            if (packet.insultUnlocked()) choices.add(new Choice(Component.literal("Insult"), () -> screen.send(NpcActionPacket.Action.INSULT), false));
            if (packet.challengeUnlocked()) {
                if (packet.mortalAvailable()) {
                    choices.add(new Choice(Component.literal("Mortal Confrontation"), screen::confirmMortal, true));
                } else {
                    choices.add(new Choice(Component.literal("Challenge to Duel"), () -> {
                        screen.send(NpcActionPacket.Action.CHALLENGE);
                        screen.onClose();
                    }, false));
                }
            }
        } else if (!packet.mortalActive()) {
            choices.add(new Choice(Component.translatable("gui.ascension.npc.withdraw"), () -> {
                screen.send(NpcActionPacket.Action.WITHDRAW);
                screen.onClose();
            }, false));
        }
        choices.add(new Choice(Component.translatable("gui.ascension.npc.leave"), screen::onClose, false));
        return choices;
    }

    private void nextPage() {
        messagePage = (messagePage + 1) % Math.max(1,
                (messageLines.size() + NpcDialogueStyle.MESSAGE_LINES_PER_PAGE - 1) / NpcDialogueStyle.MESSAGE_LINES_PER_PAGE);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        NpcDialogueStyle.PANEL.renderAt(graphics, 0, NpcDialogueStyle.PANEL_Y);
        NpcDialogueStyle.NAMEPLATE.renderAt(graphics, 22, 0);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, -3.0F);
        graphics.pose().scale(0.80F, 0.80F);
        NpcDialogueStyle.ORNAMENT.render(graphics);
        graphics.pose().popMatrix();

        var font = Minecraft.getInstance().font;
        fit(graphics, packet.name(), NpcDialogueStyle.NAME_X, NpcDialogueStyle.NAME_Y,
                NpcDialogueStyle.NAME_MAX_WIDTH, 0.91F, NpcDialogueStyle.NAME_COLOR);
        int panelY = NpcDialogueStyle.PANEL_Y;
        fit(graphics, "Realm: " + packet.realm(), NpcDialogueStyle.PANEL_TEXT_X,
                panelY + NpcDialogueStyle.REALM_Y, NpcDialogueStyle.PANEL_TEXT_WIDTH, 0.80F, NpcDialogueStyle.HIGHLIGHT_COLOR);
        fit(graphics, packet.personality() + "  |  " + packet.affiliation(), NpcDialogueStyle.PANEL_TEXT_X,
                panelY + NpcDialogueStyle.DETAILS_Y, NpcDialogueStyle.PANEL_TEXT_WIDTH, 0.78F, NpcDialogueStyle.SECONDARY_COLOR);
        fit(graphics, "Standing: " + packet.standing() + " (" + packet.respect() + ")",
                NpcDialogueStyle.PANEL_TEXT_X, panelY + NpcDialogueStyle.RELATIONSHIP_Y,
                NpcDialogueStyle.PANEL_TEXT_WIDTH, 0.78F, NpcDialogueStyle.SECONDARY_COLOR);
        graphics.fill(12, panelY + 45, 244, panelY + 46, 0x5541BDE5);

        int start = messagePage * NpcDialogueStyle.MESSAGE_LINES_PER_PAGE;
        for (int i = 0; i < NpcDialogueStyle.MESSAGE_LINES_PER_PAGE && start + i < messageLines.size(); i++) {
            graphics.text(font, messageLines.get(start + i), NpcDialogueStyle.PANEL_TEXT_X,
                    panelY + NpcDialogueStyle.MESSAGE_Y + i * NpcDialogueStyle.MESSAGE_LINE_HEIGHT,
                    confirmingMortal ? NpcDialogueStyle.WARNING_COLOR : NpcDialogueStyle.TEXT_COLOR, false);
        }
        int pages = Math.max(1, (messageLines.size() + NpcDialogueStyle.MESSAGE_LINES_PER_PAGE - 1)
                / NpcDialogueStyle.MESSAGE_LINES_PER_PAGE);
        String footer = confirmingMortal ? "A mortal confrontation cannot be withdrawn."
                : "Talks " + packet.talks() + "   Duels " + packet.wins() + "W/" + packet.losses() + "L   Reputation " + packet.reputation();
        if (pages > 1) footer += "   " + (messagePage + 1) + "/" + pages;
        fit(graphics, footer, NpcDialogueStyle.PANEL_TEXT_X, panelY + NpcDialogueStyle.FOOTER_Y,
                NpcDialogueStyle.PANEL_TEXT_WIDTH, 0.70F, NpcDialogueStyle.SECONDARY_COLOR);
    }

    private void fit(GuiGraphicsExtractor graphics, String value, int x, int y, int maxWidth, float maxScale, int color) {
        var font = Minecraft.getInstance().font;
        float scale = Math.min(maxScale, maxWidth / (float) Math.max(1, font.width(value)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, value, 0, 0, color, false);
        graphics.pose().popMatrix();
    }

    private record Choice(Component label, Runnable action, boolean dangerous, String tooltip) {
        Choice(Component label, Runnable action, boolean dangerous) {
            this(label, action, dangerous, null);
        }
    }
}
