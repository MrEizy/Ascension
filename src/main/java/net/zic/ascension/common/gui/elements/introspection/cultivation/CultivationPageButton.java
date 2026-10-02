package net.zic.ascension.common.gui.elements.introspection.cultivation;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class CultivationPageButton extends BetterButton {
    private final CultivationDisplayContainer owner;
    private final CultivationDisplayContainer.Page page;
    private final ITextureData defaultTexture;
    private final ITextureData alternateTexture;
    private final AscensionTooltip tooltip;
    private boolean selected;

    public CultivationPageButton(
            UIFrame frame,
            CultivationDisplayContainer owner,
            CultivationDisplayContainer.Page page,
            Identifier texture
    ) {
        super(frame, 0, 0);
        this.owner = owner;
        this.page = page;
        this.defaultTexture = new TextureDataSubsection(texture, 24, 48, 0, 0, 24, 24);
        this.alternateTexture = new TextureDataSubsection(texture, 24, 48, 0, 24, 24, 24);
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
        setZIndex(10);
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick() {
        owner.openPage(page);
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
            tooltip.setText(Component.translatable(
                    page == CultivationDisplayContainer.Page.PATHS
                            ? "gui.ascension.introspection.paths"
                            : "gui.ascension.introspection.techniques"
            ));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
