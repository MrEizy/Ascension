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
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.info.DescriptionDisplayContainer;

import java.util.Comparator;
import java.util.List;

public class MainContainer extends RenderableElement {
    private enum InformationPanel {
        DEFAULT,
        PHYSIQUE,
        BLOODLINES
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 256, 260, 0, 40, 234, 140
    );

    private final DescriptionDisplayContainer informationDisplay;
    private final PhysiqueOpenButton physiqueButton;
    private final BloodlineOpenButton bloodlineButton;

    private InformationPanel informationPanel = InformationPanel.DEFAULT;
    private OriginSource observedSource;
    private long observedRevision = Long.MIN_VALUE;
    private String observedIdentitySignature;

    public MainContainer(UIFrame frame) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        informationDisplay = new DescriptionDisplayContainer(
                frame,
                126,
                67,
                Component.translatable("gui.ascension.introspection.main"),
                Component.translatable("gui.ascension.introspection.select_identity")
        );
        informationDisplay.getPositioning().setX(94);
        informationDisplay.getPositioning().setY(51);
        addChild(informationDisplay);

        physiqueButton = new PhysiqueOpenButton(frame, this);
        physiqueButton.getPositioning().setX(112);
        physiqueButton.getPositioning().setY(18);
        addChild(physiqueButton);

        bloodlineButton = new BloodlineOpenButton(frame, this);
        bloodlineButton.getPositioning().setX(112);
        bloodlineButton.getPositioning().setY(35);
        addChild(bloodlineButton);

        showDefaultInformation();
        refreshSynchronizedState();
    }

    public void displayPhysique() {
        informationPanel = InformationPanel.PHYSIQUE;
        showPhysiqueInformation();
    }

    public void displayBloodlines() {
        informationPanel = InformationPanel.BLOODLINES;
        showBloodlineInformation();
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);
        long revision = source == null ? -1L : source.getRevision();
        if (source == observedSource && revision == observedRevision) {
            return;
        }

        observedSource = source;
        observedRevision = revision;

        String identitySignature = createIdentitySignature(source);
        if (identitySignature.equals(observedIdentitySignature)) {
            return;
        }
        observedIdentitySignature = identitySignature;

        physiqueButton.refreshTitle();
        bloodlineButton.refreshTitle();

        switch (informationPanel) {
            case DEFAULT -> showDefaultInformation();
            case PHYSIQUE -> showPhysiqueInformation();
            case BLOODLINES -> showBloodlineInformation();
        }
    }

    private static String createIdentitySignature(OriginSource source) {
        if (source == null) {
            return "unavailable";
        }

        StringBuilder signature = new StringBuilder();
        signature.append(source.getPhysique()).append('|');
        source.getBloodlines().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .forEach(id -> {
                    BloodlineData data = source.getBloodlineData(id);
                    signature.append(id)
                            .append(':')
                            .append(data == null ? "null" : data.getPurity())
                            .append(';');
                });
        return signature.toString();
    }

    private void showDefaultInformation() {
        showInformation(
                Component.translatable("gui.ascension.introspection.main"),
                Component.translatable("gui.ascension.introspection.select_identity")
        );
    }

    private void showPhysiqueInformation() {
        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            Identifier physiqueId = source.getPhysique();
            if (physiqueId == null) {
                showInformation(
                        Component.translatable("gui.ascension.introspection.physique"),
                        Component.translatable("gui.ascension.introspection.no_physique")
                );
                return;
            }

            ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
                Physique physique = CoreRegistries.safeAccess(
                        CoreRegistries.PHYSIQUE_REGISTRY,
                        physiqueId,
                        player.registryAccess()
                );
                if (physique == null) {
                    showInformation(
                            Component.literal(physiqueId.toString()),
                            Component.translatable(
                                    "gui.ascension.introspection.missing_registry_entry"
                            )
                    );
                    return;
                }

                Component description = physique.description() == null
                        ? Component.empty()
                        : physique.description();
                showInformation(physique.name(), description);
            }, () -> showInformation(
                    Component.literal(physiqueId.toString()),
                    Component.translatable("gui.ascension.introspection.data_unavailable")
            ));
        }, () -> showInformation(
                Component.translatable("gui.ascension.introspection.physique"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showBloodlineInformation() {
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

            ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
                if (ids.size() == 1) {
                    Identifier id = ids.getFirst();
                    Bloodline bloodline = CoreRegistries.safeAccess(
                            CoreRegistries.BLOODLINE_REGISTRY,
                            id,
                            player.registryAccess()
                    );
                    if (bloodline == null) {
                        showInformation(
                                Component.literal(id.toString()),
                                Component.translatable(
                                        "gui.ascension.introspection.missing_registry_entry"
                                )
                        );
                        return;
                    }

                    MutableComponent description = Component.empty();
                    BloodlineData data = source.getBloodlineData(id);
                    if (data != null) {
                        description.append(Component.translatable(
                                "gui.ascension.introspection.bloodline_purity",
                                data.getPurity()
                        ));
                    }
                    Component bloodlineDescription = bloodline.getDescription();
                    if (bloodlineDescription != null) {
                        if (!description.getString().isEmpty()) {
                            description.append("\n\n");
                        }
                        description.append(bloodlineDescription);
                    }
                    showInformation(bloodline.getName(), description);
                    return;
                }

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

                    description.append(
                            bloodline == null
                                    ? Component.literal(id.toString())
                                    : bloodline.getName()
                    );
                    if (data != null) {
                        description.append("\n").append(Component.translatable(
                                "gui.ascension.introspection.bloodline_purity",
                                data.getPurity()
                        ));
                    }
                    if (bloodline != null && bloodline.getDescription() != null) {
                        description.append("\n").append(bloodline.getDescription());
                    }
                }

                showInformation(
                        Component.translatable("gui.ascension.introspection.bloodlines"),
                        description
                );
            }, () -> showInformation(
                    Component.translatable("gui.ascension.introspection.bloodlines"),
                    Component.translatable("gui.ascension.introspection.data_unavailable")
            ));
        }, () -> showInformation(
                Component.translatable("gui.ascension.introspection.bloodlines"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showInformation(Component title, Component description) {
        informationDisplay.setInformation(title, description);
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        refreshSynchronizedState();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
