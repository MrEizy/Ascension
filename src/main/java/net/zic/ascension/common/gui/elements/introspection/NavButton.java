package net.zic.ascension.common.gui.elements.introspection;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class NavButton extends BetterButton {
    private final IntrospectionContainer owner;
    private final IntrospectionContainer.Panel panel;
    private final ITextureData defaultTexture;
    private final ITextureData alternateTexture;
    private final AscensionTooltip tooltip;
    private boolean selected;

    public NavButton(
            UIFrame frame,
            IntrospectionContainer owner,
            IntrospectionContainer.Panel panel,
            Identifier texture
    ) {
        super(frame, 0, 0);
        this.owner = owner;
        this.panel = panel;
        this.defaultTexture = new TextureDataSubsection(texture, 24, 48, 0, 0, 24, 24);
        this.alternateTexture = new TextureDataSubsection(texture, 24, 48, 0, 24, 24, 24);
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick() {
        owner.openPanel(panel);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (selected) {
            alternateTexture.renderAt(graphics, 0, -2);
        } else if (isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }

        if (isHovered()) {
            tooltip.setText(Component.translatable(switch (panel) {
                case MAIN -> "gui.ascension.introspection.main";
                case STATS -> "gui.ascension.introspection.stats";
                case SKILLS -> "gui.ascension.introspection.skills";
                case CULTIVATION -> "gui.ascension.introspection.cultivation";
            }));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
