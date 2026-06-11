package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.Container;
import net.zic.ascension.common.gui.elements.info.DescriptionDisplayContainer;

import java.util.Comparator;
import java.util.List;

public class MainContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 256, 260, 0, 40, 234, 140
    );

    private final Container informationContainer;

    public MainContainer(UIFrame frame) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        informationContainer = new Container(frame, 126, 67);
        informationContainer.getPositioning().setX(94);
        informationContainer.getPositioning().setY(51);
        addChild(informationContainer);

        PhysiqueOpenButton physiqueButton = new PhysiqueOpenButton(frame, this);
        physiqueButton.getPositioning().setX(112);
        physiqueButton.getPositioning().setY(18);
        addChild(physiqueButton);

        BloodlineOpenButton bloodlineButton = new BloodlineOpenButton(frame, this);
        bloodlineButton.getPositioning().setX(112);
        bloodlineButton.getPositioning().setY(35);
        addChild(bloodlineButton);

        showInformation(
                Component.translatable("gui.ascension.introspection.main"),
                Component.translatable("gui.ascension.introspection.select_identity")
        );
    }

    public void displayPhysique() {
        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            Identifier physiqueId = source.getPhysique();
            if (physiqueId == null) {
                showInformation(
                        Component.translatable("gui.ascension.introspection.physique"),
                        Component.translatable("gui.ascension.introspection.no_physique")
                );
                return;
            }

            ClientAscensionData.getPlayer().ifPresent(player -> {
                Physique physique = CoreRegistries.safeAccess(
                        CoreRegistries.PHYSIQUE_REGISTRY,
                        physiqueId,
                        player.registryAccess()
                );
                if (physique == null) {
                    showInformation(
                            Component.literal(physiqueId.toString()),
                            Component.translatable("gui.ascension.introspection.missing_registry_entry")
                    );
                    return;
                }
                showInformation(physique.name(), physique.description());
            });
        }, () -> showInformation(
                Component.translatable("gui.ascension.introspection.physique"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    public void displayBloodlines() {
        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            List<Identifier> ids = source.getBloodlines().stream()
                    .sorted(Comparator.comparing(Identifier::toString))
                    .toList();
            if (ids.isEmpty()) {
                showInformation(
                        Component.translatable("gui.ascension.introspection.bloodlines"),
                        Component.translatable("gui.ascension.introspection.no_bloodlines")
                );
                return;
            }

            ClientAscensionData.getPlayer().ifPresent(player -> {
                MutableComponent description = Component.empty();
                for (int index = 0; index < ids.size(); index++) {
                    Identifier id = ids.get(index);
                    Bloodline bloodline = CoreRegistries.safeAccess(
                            CoreRegistries.BLOODLINE_REGISTRY,
                            id,
                            player.registryAccess()
                    );
                    BloodlineData data = source.getBloodlineData(id);
                    if (index > 0) {
                        description.append("\n\n");
                    }
                    if (bloodline == null) {
                        description.append(Component.literal(id.toString()));
                    } else {
                        description.append(bloodline.getName());
                    }
                    if (data != null) {
                        description.append(Component.literal(" (" + data.getPurity() + "%)"));
                    }
                    if (bloodline != null && bloodline.getDescription() != null) {
                        description.append("\n").append(bloodline.getDescription());
                    }
                }
                showInformation(
                        Component.translatable("gui.ascension.introspection.bloodlines"),
                        description
                );
            });
        }, () -> showInformation(
                Component.translatable("gui.ascension.introspection.bloodlines"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showInformation(Component title, Component description) {
        informationContainer.removeChildren();
        DescriptionDisplayContainer display = new DescriptionDisplayContainer(
                getUiFrame(),
                title,
                description
        );
        informationContainer.addChild(display);
        display.refresh();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
