package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class PhysiqueOpenButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/text_buttons.png"
    );

    private final MainContainer owner;
    private final EasyLabel titleLabel;
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 0, 89, 12
    );
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 12, 89, 12
    );

    public PhysiqueOpenButton(UIFrame frame, MainContainer owner) {
        super(frame, 0, 0);
        this.owner = owner;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());

        titleLabel = new EasyLabel(frame);
        titleLabel.setText(Component.empty());
        titleLabel.getPositioning().setX(2);
        titleLabel.getPositioning().setY(2);
        titleLabel.setWidth(85);
        titleLabel.setHeight(8);
        titleLabel.setScaleToFit(true);
        titleLabel.setTextColor(0xFFFFFFFF);
        titleLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        titleLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(titleLabel);

        refreshTitle();
    }

    public void refreshTitle() {
        titleLabel.setText(resolveTitle());
        titleLabel.setTextScale(1.0F);
    }

    private static Component resolveTitle() {
        return ClientAscensionData.getSource().flatMap(source -> {
            Identifier physiqueId = source.getPhysique();
            if (physiqueId == null) {
                return java.util.Optional.empty();
            }
            return ClientAscensionData.getPlayer().map(player ->
                    CoreRegistries.safeAccess(
                            CoreRegistries.PHYSIQUE_REGISTRY,
                            physiqueId,
                            player.registryAccess()
                    )
            );
        }).map(Physique::name).orElseGet(() ->
                Component.translatable("gui.ascension.introspection.no_physique")
        );
    }

    @Override
    public void onClick() {
        owner.displayPhysique();
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
