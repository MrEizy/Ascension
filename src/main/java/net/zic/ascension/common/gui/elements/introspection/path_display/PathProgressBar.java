package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;

import java.text.DecimalFormat;
import java.util.Optional;

public class PathProgressBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );

    private static final ITextureData PROGRESS_TEXTURE =
            new TextureDataSubsection(
                    TEXTURE,
                    234,
                    287,
                    1,
                    181,
                    84,
                    9
            );

    private static final DecimalFormat PROGRESS_FORMAT = new DecimalFormat("#,##0.##");

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");

    private final AscensionTooltip tooltip;

    private Identifier selectedPath;

    public PathProgressBar(UIFrame frame) {
        super(frame);

        setWidth(PROGRESS_TEXTURE.getWidth());
        setHeight(PROGRESS_TEXTURE.getHeight());

        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    public void setPath(Identifier pathId) {
        selectedPath = pathId;
    }

    private Optional<PathProgressState> getProgressState() {
        if (selectedPath == null) {
            return Optional.empty();
        }

        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSource().flatMap(source -> {
                    PathData pathData = source.getPathData(selectedPath);
                    if (pathData == null) {
                        return Optional.empty();
                    }

                    double current = pathData.getProgress();

                    double maximum = pathData.getMaxProgress(
                            pathData.getMajorRealm(),
                            pathData.getMinorRealm(),
                            player.registryAccess()
                    );

                    double progress = maximum <= 0.0D ? 0.0D : Math.clamp(current / maximum, 0.0D, 1.0D);

                    return Optional.of(new PathProgressState(current, progress));
                })
        );
    }

    private void showTooltip(PathProgressState state) {
        double percentage = state.progress() * 100.0D;

        tooltip.setText(Component.translatable(
                "gui.ascension.path_progress.tooltip",
                PROGRESS_FORMAT.format(state.current()),
                PERCENT_FORMAT.format(percentage)
        ));

        getUiFrame().setTooltip(tooltip);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Optional<PathProgressState> optionalState = getProgressState();
        if (optionalState.isEmpty()) {
            return;
        }

        PathProgressState state = optionalState.get();

        int width = (int) Math.round(
                PROGRESS_TEXTURE.getWidth() * state.progress()
        );

        if (width > 0) {
            PROGRESS_TEXTURE.render(graphics, width, PROGRESS_TEXTURE.getHeight());
        }

        if (isPointBounded(graphics, mouseX, mouseY)) {
            showTooltip(state);
        }
    }

    private record PathProgressState(double current, double progress) {
    }
}