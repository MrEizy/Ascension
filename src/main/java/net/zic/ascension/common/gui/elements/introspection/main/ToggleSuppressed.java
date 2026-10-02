package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.network.ToggleCultivationSuppressedPacket;

public class ToggleSuppressed extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    private static final ITextureData TICKED = new TextureDataSubsection(
            TEXTURE,
            256, 260,
            12, 232,
            9, 8
    );

    private final AscensionTooltip tooltip;

    public ToggleSuppressed(UIFrame frame, int x, int y) {
        super(frame, x, y);
        setWidth(13);
        setHeight(13);
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    public boolean isToggled() {
        return ClientAscensionData.getEntityData()
                .map(data -> data.isCultivationSuppressed())
                .orElse(false);
    }

    @Override
    public void onClick() {
        super.onClick();

        boolean nextState = !isToggled();

        ClientPacketDistributor.sendToServer(
                new ToggleCultivationSuppressedPacket(nextState)
        );

        ClientAscensionData.getEntityData().ifPresent(data ->
                data.setCultivationSuppressed(nextState)
        );
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (isToggled()) {
            TICKED.renderAt(graphics, 2, 3);
        }

        if (isHovered()) {
            graphics.fill(0, 0, getWidth(), getHeight(), 0x64999999);
            tooltip.setText(Component.translatable(
                    isToggled()
                            ? "gui.ascension.introspection.cultivation_suppression.disable"
                            : "gui.ascension.introspection.cultivation_suppression.enable"
            ));
            getUiFrame().setTooltip(tooltip);
        }
    }
}