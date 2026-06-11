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
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.Container;
import net.zic.ascension.common.gui.elements.info.PathDataDisplayElement;
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

    private final Container pathInformationContainer;
    private final EasyLabel selectedTechniqueLabel;
    private final PathProgressBar progressBar;

    public PathDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        PathOptionsScrollBox options = new PathOptionsScrollBox(frame);
        options.getPositioning().setX(6);
        options.getPositioning().setY(43);
        addChild(options);

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

        List<Identifier> paths = ClientAscensionData.getSource()
                .map(source -> source.getPaths().stream()
                        .sorted(Comparator.comparing(Identifier::toString))
                        .toList())
                .orElse(List.of());

        for (Identifier pathId : paths) {
            options.addChild(new PathSelectionButton(frame, this, pathId));
        }

        if (paths.isEmpty()) {
            showEmptyState();
        } else {
            selectPath(paths.getFirst());
        }
    }

    public void selectPath(Identifier pathId) {
        progressBar.setPath(pathId);
        pathInformationContainer.removeChildren();

        ClientAscensionData.getSource().ifPresentOrElse(source -> {
            PathData pathData = source.getPathData(pathId);
            if (pathData == null) {
                showMissingPath(pathId);
                return;
            }

            ClientAscensionData.getPlayer().ifPresent(player -> {
                Path path = CoreRegistries.safeAccess(
                        CoreRegistries.PATH_REGISTRY,
                        pathId,
                        player.registryAccess()
                );
                if (path == null) {
                    showMissingPath(pathId);
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

                MutableComponent description = Component.empty().append(path.description());
                if (technique != null) {
                    description.append("\n\n")
                            .append(Component.translatable("gui.ascension.introspection.technique"))
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
            });
        }, () -> showMissingPath(pathId));
    }

    private void showEmptyState() {
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        PathDataDisplayElement display = new PathDataDisplayElement(
                getUiFrame(),
                Component.translatable("gui.ascension.introspection.no_paths"),
                Component.translatable("gui.ascension.introspection.no_paths_description")
        );
        pathInformationContainer.addChild(display);
        display.refresh();
    }

    private void showMissingPath(Identifier pathId) {
        selectedTechniqueLabel.setText(Component.translatable("gui.ascension.introspection.none"));
        PathDataDisplayElement display = new PathDataDisplayElement(
                getUiFrame(),
                Component.literal(pathId.toString()),
                Component.translatable("gui.ascension.introspection.missing_registry_entry")
        );
        pathInformationContainer.addChild(display);
        display.refresh();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
