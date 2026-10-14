package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;

public final class NpcDialogueChoiceButton extends BetterButton {
    private final Component text;
    private final Runnable action;
    private final float scale;
    private final boolean enabled;
    private final boolean dangerous;
    private final AscensionTooltip tooltip;
    private final Component hint;

    public NpcDialogueChoiceButton(UIFrame frame, Component text, Runnable action, float scale, boolean enabled, boolean dangerous, String hint) {
        super(frame, 0, 0);
        this.text = text;
        this.action = action;
        this.scale = scale;
        this.enabled = enabled;
        this.dangerous = dangerous;
        this.hint = hint == null || hint.isBlank() ? null : Component.literal(hint);
        this.tooltip = this.hint == null ? null : new AscensionTooltip(frame);
        if (tooltip != null) tooltip.setActive(true);
        setWidth(Math.round(NpcDialogueStyle.BUTTON_WIDTH * scale));
        setHeight(NpcDialogueStyle.BUTTON_HEIGHT);
    }

    @Override
    public void onClick() {
        if (enabled) action.run();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ITextureData texture = !enabled ? NpcDialogueStyle.BUTTON_DISABLED
                : isHovered() || isPressed() ? NpcDialogueStyle.BUTTON_HOVER : NpcDialogueStyle.BUTTON_NORMAL;
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, 1.0F);
        texture.render(graphics);
        graphics.pose().popMatrix();

        int color = !enabled ? NpcDialogueStyle.SECONDARY_COLOR
                : dangerous ? NpcDialogueStyle.WARNING_COLOR : isHovered() ? NpcDialogueStyle.HIGHLIGHT_COLOR : NpcDialogueStyle.TEXT_COLOR;
        var font = Minecraft.getInstance().font;
        float textScale = Math.min(0.90F, (getWidth() - 12.0F) / Math.max(1, font.width(text)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(getWidth() / 2.0F, 6.0F);
        graphics.pose().scale(textScale, textScale);
        graphics.centeredText(font, text, 0, 0, color);
        graphics.pose().popMatrix();
    }
}
