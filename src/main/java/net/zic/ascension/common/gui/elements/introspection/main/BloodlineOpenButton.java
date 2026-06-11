package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class BloodlineOpenButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/text_buttons.png"
    );

    private final MainContainer owner;
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 0, 89, 12
    );
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 12, 89, 12
    );

    public BloodlineOpenButton(UIFrame frame, MainContainer owner) {
        super(frame, 0, 0);
        this.owner = owner;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());

        int count = ClientAscensionData.getSource()
                .map(source -> source.getBloodlines().size())
                .orElse(0);

        EasyLabel title = new EasyLabel(frame);
        title.setText(Component.translatable("gui.ascension.introspection.bloodline_count", count));
        title.getPositioning().setX(2);
        title.getPositioning().setY(2);
        title.setWidth(85);
        title.setHeight(8);
        title.setScaleToFit(true);
        title.setTextColor(0xFFFFFFFF);
        title.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        title.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(title);
    }

    @Override
    public void onClick() {
        owner.displayBloodlines();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
