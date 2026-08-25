package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class OpenActiveSelection extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );

    private final SkillBarContainer skillBar;
    private final ITextureData openDefaultTexture = new TextureDataSubsection(
            TEXTURE, 234, 286, 167, 241, 13, 11
    );
    private final ITextureData openAlternateTexture = new TextureDataSubsection(
            TEXTURE, 234, 286, 167, 252, 13, 11
    );
    private final ITextureData closeDefaultTexture = new TextureDataSubsection(
            TEXTURE, 234, 286, 154, 241, 13, 11
    );
    private final ITextureData closeAlternateTexture = new TextureDataSubsection(
            TEXTURE, 234, 286, 154, 252, 13, 11
    );

    private final AscensionTooltip tooltip;
    private boolean open;

    public OpenActiveSelection(UIFrame frame, SkillBarContainer skillBar, int x, int y) {
        super(frame, x, y);
        this.skillBar = skillBar;
        setWidth(openDefaultTexture.getWidth());
        setHeight(openDefaultTexture.getHeight());
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    @Override
    public void onClick() {
        open = !open;
        skillBar.setActive(open);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (open) {
            if (isHovered() || isPressed()) {
                closeAlternateTexture.render(graphics);
            } else {
                closeDefaultTexture.render(graphics);
            }
        } else if (isHovered() || isPressed()) {
            openAlternateTexture.render(graphics);
        } else {
            openDefaultTexture.render(graphics);
        }

        if (isHovered()) {
            tooltip.setText(Component.translatable(
                    open
                            ? "gui.ascension.introspection.skill_slots.close"
                            : "gui.ascension.introspection.skill_slots.open"
            ));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
