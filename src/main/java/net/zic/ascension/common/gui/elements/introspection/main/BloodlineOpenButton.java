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
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

import java.util.Comparator;
import java.util.List;

public class BloodlineOpenButton extends BetterButton {
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

    public BloodlineOpenButton(UIFrame frame, MainContainer owner) {
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
        List<Identifier> bloodlines = ClientAscensionData.getSource()
                .map(source -> source.getBloodlines().stream()
                        .sorted(Comparator.comparing(Identifier::toString))
                        .toList())
                .orElse(List.of());

        if (bloodlines.isEmpty()) {
            return Component.translatable("gui.ascension.introspection.no_bloodlines");
        }

        Identifier firstId = bloodlines.getFirst();
        Component firstName = ClientAscensionData.getPlayer().map(player -> {
            Bloodline bloodline = CoreRegistries.safeAccess(
                    CoreRegistries.BLOODLINE_REGISTRY,
                    firstId,
                    player.registryAccess()
            );
            return bloodline == null ? Component.literal(firstId.toString()) : bloodline.getName();
        }).orElseGet(() -> Component.literal(firstId.toString()));

        if (bloodlines.size() == 1) {
            return firstName;
        }

        return Component.translatable(
                "gui.ascension.introspection.bloodline_multiple",
                firstName,
                bloodlines.size() - 1
        );
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
