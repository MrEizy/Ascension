package net.zic.ascension.common.gui.elements.introspection.technique_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class TechniqueSelectionButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/technique_menu/technique_text_buttons.png"
    );

    private final TechniqueDisplayContainer owner;
    private final Identifier techniqueId;
    private final EasyLabel label;
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 0, 89, 12
    );
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 12, 89, 12
    );

    public TechniqueSelectionButton(
            UIFrame frame,
            TechniqueDisplayContainer owner,
            Identifier techniqueId
    ) {
        super(frame, 0, 0);
        this.owner = owner;
        this.techniqueId = techniqueId;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());

        label = new EasyLabel(frame);
        label.setText(Component.empty());
        label.setTextColor(0xFFFFFFFF);
        label.setWidth(85);
        label.setHeight(8);
        label.getPositioning().setX(2);
        label.getPositioning().setY(2);
        label.setScaleToFit(true);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(label);
        refreshTitle();
    }

    public Identifier getTechniqueId() {
        return techniqueId;
    }

    public void refreshTitle() {
        label.setText(resolveName(techniqueId));
        label.setTextScale(1.0F);
    }

    private static Component resolveName(Identifier techniqueId) {
        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSource().map(source -> {
                    Technique technique = CoreRegistries.safeAccess(
                            CoreRegistries.TECHNIQUE_REGISTRY,
                            techniqueId,
                            player.registryAccess()
                    );
                    return technique == null
                            ? Component.literal(techniqueId.toString())
                            : technique.getName(AscensionOriginSourceHelper.getTechniqueData(source, techniqueId));
                })
        ).orElseGet(() -> Component.literal(techniqueId.toString()));
    }

    @Override
    public void onClick() {
        owner.selectTechnique(techniqueId);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (owner.isSelectedTechnique(techniqueId) || isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
