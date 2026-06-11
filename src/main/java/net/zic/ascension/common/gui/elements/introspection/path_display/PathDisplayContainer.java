package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.Container;
import net.zic.ascension.common.gui.elements.info.PathDataDisplayElement;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

import java.util.Comparator;
import java.util.List;

public class PathDisplayContainer extends RenderableElement {
    private record PathDetailsState(
            Identifier pathId,
            int majorRealm,
            int minorRealm,
            Identifier techniqueId
    ) {
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 287, 0, 39, 234, 140
    );

    private final Container optionsHolder;
    private final Container pathInformationContainer;
    private final EasyLabel selectedTechniqueLabel;
    private final PathProgressBar progressBar;

    private OriginSource observedSource;
    private long observedRevision = Long.MIN_VALUE;
    private List<Identifier> displayedPaths = List.of();
    private Identifier selectedPath;
    private PathDetailsState displayedPathDetails;

    public PathDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        optionsHolder = new Container(frame, 89, 91);
        optionsHolder.getPositioning().setX(6);
        optionsHolder.getPositioning().setY(43);
        addChild(optionsHolder);

        selectedTechniqueLabel = new EasyLabel(frame);
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        selectedTechniqueLabel.setTextColor(0xFFFFFFFF);
        selectedTechniqueLabel.setScaleToFit(true);
        selectedTechniqueLabel.setWidth(78);
        selectedTechniqueLabel.setHeight(8);
        selectedTechniqueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        selectedTechniqueLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        selectedTechniqueLabel.getPositioning().setX(131);
        selectedTechniqueLabel.getPositioning().setY(16);
        addChild(selectedTechniqueLabel);

        pathInformationContainer = new Container(frame, 94, 85);
        pathInformationContainer.getPositioning().setX(123);
        pathInformationContainer.getPositioning().setY(46);
        addChild(pathInformationContainer);

        progressBar = new PathProgressBar(frame);
        progressBar.getPositioning().setX(127);
        progressBar.getPositioning().setY(31);
        addChild(progressBar);

        BackButton backButton = new BackButton(frame, owner);
        backButton.getPositioning().setX(5);
        backButton.getPositioning().setY(5);
        addChild(backButton);

        refreshSynchronizedState();
    }

    public void selectPath(Identifier pathId) {
        selectedPath = pathId;
        progressBar.setPath(pathId);
        displayedPathDetails = null;
        refreshSelectedPath(true);
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);
        long revision = source == null ? -1L : source.getRevision();
        if (source == observedSource && revision == observedRevision) {
            return;
        }

        boolean sourceChanged = source != observedSource;
        observedSource = source;
        observedRevision = revision;

        if (source == null) {
            displayedPaths = List.of();
            selectedPath = null;
            progressBar.setPath(null);
            displayedPathDetails = null;
            rebuildPathOptions(displayedPaths);
            showUnavailableState();
            return;
        }

        List<Identifier> paths = source.getPaths().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();

        if (!paths.equals(displayedPaths)) {
            displayedPaths = paths;
            rebuildPathOptions(paths);
        }

        if (paths.isEmpty()) {
            selectedPath = null;
            progressBar.setPath(null);
            displayedPathDetails = null;
            showEmptyState();
            return;
        }

        if (selectedPath == null || !paths.contains(selectedPath)) {
            selectedPath = paths.getFirst();
        }
        progressBar.setPath(selectedPath);
        refreshSelectedPath(sourceChanged);
    }

    private void rebuildPathOptions(List<Identifier> paths) {
        optionsHolder.removeChildren();
        PathOptionsScrollBox options = new PathOptionsScrollBox(getUiFrame());
        optionsHolder.addChild(options);
        for (Identifier pathId : paths) {
            options.addChild(new PathSelectionButton(getUiFrame(), this, pathId));
        }
    }

    private void refreshSelectedPath(boolean force) {
        if (selectedPath == null) {
            displayedPathDetails = null;
            showEmptyState();
            return;
        }

        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            PathData pathData = source.getPathData(selectedPath);
            if (pathData == null) {
                displayedPathDetails = null;
                showMissingPath(selectedPath);
                return;
            }

            PathDetailsState currentDetails = new PathDetailsState(
                    selectedPath,
                    pathData.getMajorRealm(),
                    pathData.getMinorRealm(),
                    pathData.getCurrentTechnique()
            );
            if (!force && currentDetails.equals(displayedPathDetails)) {
                return;
            }
            displayedPathDetails = currentDetails;
            pathInformationContainer.removeChildren();

            ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
                Path path = CoreRegistries.safeAccess(
                        CoreRegistries.PATH_REGISTRY,
                        selectedPath,
                        player.registryAccess()
                );
                if (path == null) {
                    showMissingPath(selectedPath);
                    return;
                }

                Identifier techniqueId = pathData.getCurrentTechnique();
                Technique technique = techniqueId == null ? null : CoreRegistries.safeAccess(
                        CoreRegistries.TECHNIQUE_REGISTRY,
                        techniqueId,
                        player.registryAccess()
                );
                selectedTechniqueLabel.setText(
                        technique == null
                                ? Component.translatable("gui.ascension.introspection.none")
                                : technique.getName(pathData.getCurrentTechniqueData())
                );

                MutableComponent description = Component.empty();
                if (path.description() != null) {
                    description.append(path.description());
                }
                if (technique != null) {
                    if (!description.getString().isEmpty()) {
                        description.append("\n\n");
                    }
                    description.append(Component.translatable(
                                    "gui.ascension.introspection.technique"
                            ))
                            .append(": ")
                            .append(technique.getName(pathData.getCurrentTechniqueData()));
                    Component techniqueDescription = technique.getDescription(
                            pathData.getCurrentTechniqueData()
                    );
                    if (techniqueDescription != null) {
                        description.append("\n").append(techniqueDescription);
                    }
                }

                PathDataDisplayElement display = new PathDataDisplayElement(
                        getUiFrame(),
                        pathData.getRealmName(
                                pathData.getMajorRealm(),
                                pathData.getMinorRealm(),
                                player.registryAccess()
                        ),
                        description
                );
                pathInformationContainer.addChild(display);
                display.refresh();
            }, this::showUnavailableState);
        }, this::showUnavailableState);
    }

    private void showEmptyState() {
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        replaceInformation(new PathDataDisplayElement(
                getUiFrame(),
                Component.translatable("gui.ascension.introspection.no_paths"),
                Component.translatable("gui.ascension.introspection.no_paths_description")
        ));
    }

    private void showUnavailableState() {
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        replaceInformation(new PathDataDisplayElement(
                getUiFrame(),
                Component.translatable("gui.ascension.introspection.cultivation"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showMissingPath(Identifier pathId) {
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        replaceInformation(new PathDataDisplayElement(
                getUiFrame(),
                Component.literal(pathId.toString()),
                Component.translatable("gui.ascension.introspection.missing_registry_entry")
        ));
    }

    private void replaceInformation(PathDataDisplayElement display) {
        pathInformationContainer.removeChildren();
        pathInformationContainer.addChild(display);
        display.refresh();
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
