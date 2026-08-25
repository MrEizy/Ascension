package net.zic.ascension.common.gui.elements.introspection.technique_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.info.DescriptionDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

import java.util.Comparator;
import java.util.List;

public class TechniqueDisplayContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/technique_menu/technique_menu.png"
    );
    private static final Identifier CULTIVATION_TEMPLATE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "default_cultivation_technique"
    );
    private static final Identifier BATTLE_STYLE_TEMPLATE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "default_battle_style"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 287, 0, 39, 234, 140
    );

    private final TechniqueOptionsScrollBox techniqueOptions;
    private final EasyLabel selectedPathLabel;
    private final DescriptionDisplayContainer techniqueInformation;

    private List<Identifier> displayedTechniques = List.of();
    private Identifier selectedTechnique;

    public TechniqueDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        techniqueOptions = new TechniqueOptionsScrollBox(frame);
        techniqueOptions.getPositioning().setX(6);
        techniqueOptions.getPositioning().setY(43);
        addChild(techniqueOptions);

        selectedPathLabel = createTopLabel(frame, 31);
        addChild(selectedPathLabel);

        techniqueInformation = new DescriptionDisplayContainer(
                frame,
                94,
                85,
                Component.translatable("gui.ascension.introspection.technique"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        );
        techniqueInformation.getPositioning().setX(123);
        techniqueInformation.getPositioning().setY(46);
        addChild(techniqueInformation);

        BackButton backButton = new BackButton(frame, owner);
        backButton.getPositioning().setX(5);
        backButton.getPositioning().setY(5);
        addChild(backButton);

        refreshSynchronizedState();
    }

    private static EasyLabel createTopLabel(UIFrame frame, int y) {
        EasyLabel label = new EasyLabel(frame);
        label.setTextColor(0xFFFFFFFF);
        label.setWidth(78);
        label.setHeight(8);
        label.setScaleToFit(true);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        label.getPositioning().setX(131);
        label.getPositioning().setY(y);
        label.setText(Component.translatable("gui.ascension.introspection.none"));
        return label;
    }

    public boolean isSelectedTechnique(Identifier techniqueId) {
        return techniqueId != null && techniqueId.equals(selectedTechnique);
    }

    public void selectTechnique(Identifier techniqueId) {
        if (techniqueId == null || !displayedTechniques.contains(techniqueId)) {
            return;
        }
        selectedTechnique = techniqueId;
        refreshSelectedTechnique();
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);

        if (source == null) {
            displayedTechniques = List.of();
            selectedTechnique = null;
            techniqueOptions.setTechniques(this, displayedTechniques);
            showUnavailableState();
            return;
        }

        List<Identifier> techniques = AscensionOriginSourceHelper.getTechniques(source).stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();

        if (!techniques.equals(displayedTechniques)) {
            displayedTechniques = techniques;
            techniqueOptions.setTechniques(this, techniques);
        }

        if (techniques.isEmpty()) {
            selectedTechnique = null;
            showEmptyState();
            return;
        }

        if (selectedTechnique == null || !techniques.contains(selectedTechnique)) {
            selectedTechnique = techniques.getFirst();
        }
        refreshSelectedTechnique();
    }

    private void refreshSelectedTechnique() {
        if (selectedTechnique == null) {
            showEmptyState();
            return;
        }

        ClientAscensionData.getSource().ifPresentOrElse(source ->
                ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
                    Technique technique = CoreRegistries.safeAccess(
                            CoreRegistries.TECHNIQUE_REGISTRY,
                            selectedTechnique,
                            player.registryAccess()
                    );
                    if (technique == null) {
                        showMissingTechnique(selectedTechnique);
                        return;
                    }

                    TechniqueData data = AscensionOriginSourceHelper.getTechniqueData(source, selectedTechnique);
                    setPathTitle(resolvePathName(technique.getPath(), player.registryAccess()));
                    techniqueInformation.setInformation(
                            resolveTechniqueType(technique),
                            technique.getDescription(data) == null
                                    ? Component.empty()
                                    : technique.getDescription(data)
                    );
                }, this::showUnavailableState),
                this::showUnavailableState
        );
    }

    private static Component resolvePathName(Identifier pathId, net.minecraft.core.RegistryAccess access) {
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, access);
        return path == null ? Component.literal(pathId.toString()) : path.name();
    }

    private static Component resolveTechniqueType(Technique technique) {
        Identifier template = technique.itemTooltip()
                .flatMap(definition -> definition.template())
                .orElse(null);
        if (BATTLE_STYLE_TEMPLATE.equals(template)) {
            return Component.translatable("gui.ascension.introspection.battle_style");
        }
        if (CULTIVATION_TEMPLATE.equals(template)) {
            return Component.translatable("gui.ascension.introspection.cultivation_technique");
        }
        return Component.translatable("gui.ascension.introspection.technique");
    }

    private void setPathTitle(Component title) {
        selectedPathLabel.setText(title == null ? Component.empty() : title);
        selectedPathLabel.setTextScale(1.0F);
    }

    private void showEmptyState() {
        setPathTitle(Component.translatable("gui.ascension.introspection.none"));
        techniqueInformation.setInformation(
                Component.translatable("gui.ascension.introspection.no_techniques"),
                Component.translatable("gui.ascension.introspection.no_techniques_description")
        );
    }

    private void showUnavailableState() {
        setPathTitle(Component.translatable("gui.ascension.introspection.none"));
        techniqueInformation.setInformation(
                Component.translatable("gui.ascension.introspection.techniques"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        );
    }

    private void showMissingTechnique(Identifier techniqueId) {
        setPathTitle(Component.translatable("gui.ascension.introspection.none"));
        techniqueInformation.setInformation(
                Component.literal(techniqueId.toString()),
                Component.translatable("gui.ascension.introspection.missing_registry_entry")
        );
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
