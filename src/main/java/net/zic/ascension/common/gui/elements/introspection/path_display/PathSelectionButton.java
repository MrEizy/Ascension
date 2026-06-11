package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class PathSelectionButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_text_buttons.png"
    );

    private final PathDisplayContainer owner;
    private final Identifier pathId;
    private final EasyLabel label;
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 0, 89, 12
    );
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 12, 89, 12
    );

    public PathSelectionButton(UIFrame frame, PathDisplayContainer owner, Identifier pathId) {
        super(frame, 0, 0);
        this.owner = owner;
        this.pathId = pathId;
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

    public Identifier getPathId() {
        return pathId;
    }

    public void refreshTitle() {
        label.setText(resolveName(pathId));
        label.setTextScale(1.0F);
    }

    private static Component resolveName(Identifier pathId) {
        return ClientAscensionData.getPlayer().map(player -> {
            Path path = CoreRegistries.safeAccess(
                    CoreRegistries.PATH_REGISTRY,
                    pathId,
                    player.registryAccess()
            );
            return path == null ? Component.literal(pathId.toString()) : path.name();
        }).orElseGet(() -> Component.literal(pathId.toString()));
    }

    @Override
    public void onClick() {
        owner.selectPath(pathId);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (owner.isSelectedPath(pathId) || isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
