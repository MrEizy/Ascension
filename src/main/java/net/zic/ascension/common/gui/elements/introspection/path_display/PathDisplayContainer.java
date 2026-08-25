package net.zic.ascension.common.gui.elements.introspection.path_display;

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
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.info.PathInstanceDisplayElement;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

import java.util.Comparator;
import java.util.List;

public class PathDisplayContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 287, 0, 39, 234, 140
    );

    private final PathOptionsScrollBox pathOptions;
    private final EasyLabel selectedPathLabel;
    private final PathInstanceDisplayElement pathInformation;
    private final PathProgressBar progressBar;

    private OriginSource observedSource;
    private long observedRevision = Long.MIN_VALUE;
    private List<Identifier> displayedPaths = List.of();
    private Identifier selectedPath;

    public PathDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        pathOptions = new PathOptionsScrollBox(frame);
        pathOptions.getPositioning().setX(6);
        pathOptions.getPositioning().setY(43);
        addChild(pathOptions);

        selectedPathLabel = new EasyLabel(frame);
        selectedPathLabel.setTextColor(0xFFFFFFFF);
        selectedPathLabel.setWidth(78);
        selectedPathLabel.setHeight(8);
        selectedPathLabel.setScaleToFit(true);
        selectedPathLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        selectedPathLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        selectedPathLabel.getPositioning().setX(131);
        selectedPathLabel.getPositioning().setY(16);
        selectedPathLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        addChild(selectedPathLabel);

        pathInformation = new PathInstanceDisplayElement(
                frame,
                94,
                85,
                Component.translatable("gui.ascension.introspection.cultivation"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        );
        pathInformation.getPositioning().setX(123);
        pathInformation.getPositioning().setY(46);
        addChild(pathInformation);

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

    public boolean isSelectedPath(Identifier pathId) {
        return pathId != null && pathId.equals(selectedPath);
    }

    public void selectPath(Identifier pathId) {
        if (pathId == null || !displayedPaths.contains(pathId)) {
            return;
        }
        selectedPath = pathId;
        progressBar.setPath(pathId);
        refreshSelectedPath();
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);
        observedSource = source;

        if (source == null) {
            displayedPaths = List.of();
            selectedPath = null;
            pathOptions.setPaths(this, displayedPaths);
            progressBar.setPath(null);
            showUnavailableState();
            return;
        }

        List<Identifier> paths = AscensionOriginSourceHelper.getPaths(source).stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();

        if (!paths.equals(displayedPaths)) {
            displayedPaths = paths;
            pathOptions.setPaths(this, paths);
        }

        if (paths.isEmpty()) {
            selectedPath = null;
            progressBar.setPath(null);
            showEmptyState();
            return;
        }

        if (selectedPath == null || !paths.contains(selectedPath)) {
            selectedPath = paths.getFirst();
        }
        progressBar.setPath(selectedPath);
        refreshSelectedPath();
    }

    private void refreshSelectedPath() {
        if (selectedPath == null) {
            showEmptyState();
            return;
        }

        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, selectedPath);
            if (pathInstance == null) {
                showMissingPath(selectedPath);
                return;
            }

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

                setPathTitle(path.name());
                Component description = path.description() == null
                        ? Component.empty()
                        : path.description();

                pathInformation.setInformation(
                        path.getRealmName(
                                pathInstance.getCurrentMajorRealm(),
                                pathInstance.getCurrentMinorRealm()
                        ),
                        description
                );
            }, this::showUnavailableState);
        }, this::showUnavailableState);
    }

    private void setPathTitle(Component title) {
        selectedPathLabel.setText(title == null ? Component.empty() : title);
        selectedPathLabel.setTextScale(1.0F);
    }

    private void showEmptyState() {
        setPathTitle(Component.translatable("gui.ascension.introspection.none"));
        pathInformation.setInformation(
                Component.translatable("gui.ascension.introspection.no_paths"),
                Component.translatable("gui.ascension.introspection.no_paths_description")
        );
    }

    private void showUnavailableState() {
        setPathTitle(Component.translatable("gui.ascension.introspection.none"));
        pathInformation.setInformation(
                Component.translatable("gui.ascension.introspection.cultivation"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        );
    }

    private void showMissingPath(Identifier pathId) {
        setPathTitle(Component.literal(pathId.toString()));
        pathInformation.setInformation(
                Component.literal(pathId.toString()),
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
