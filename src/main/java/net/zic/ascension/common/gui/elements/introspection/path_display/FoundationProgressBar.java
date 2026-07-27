package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

import java.util.List;
import java.util.Optional;

public class FoundationProgressBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );

    private static final List<ITextureData> TIERS = List.of(
            new TextureDataSubsection(TEXTURE, 234, 287, 175, 184, 9, 84),
            new TextureDataSubsection(TEXTURE, 234, 287, 186, 184, 9, 84),
            new TextureDataSubsection(TEXTURE, 234, 287, 197, 184, 9, 84),
            new TextureDataSubsection(TEXTURE, 234, 287, 208, 184, 9, 84),
            new TextureDataSubsection(TEXTURE, 234, 287, 208, 184, 9, 84),
            new TextureDataSubsection(TEXTURE, 234, 287, 219, 184, 9, 84)
    );

    private Identifier selectedPath;

    public FoundationProgressBar(UIFrame frame) {
        super(frame);

        setWidth(TIERS.getFirst().getWidth());
        setHeight(TIERS.getFirst().getHeight());
    }

    public void setPath(Identifier pathId) {
        selectedPath = pathId;
    }

    private Optional<FoundationState> getFoundationState() {
        if (selectedPath == null) {
            return Optional.empty();
        }

        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSource().flatMap(source -> {
                    PathData pathData = AscensionOriginSourceHelper.getPathData(source,selectedPath);
                    if (!(pathData instanceof FoundationPathData foundationData)) {
                        return Optional.empty();
                    }

                    Path path = CoreRegistries.safeAccess(
                            CoreRegistries.PATH_REGISTRY,
                            selectedPath,
                            player.registryAccess()
                    );

                    if (!(path instanceof FoundationPath foundationPath)) {
                        return Optional.empty();
                    }

                    int majorRealm = foundationData.getMajorRealm();
                    int foundationRealm = foundationData.getCurrentFoundationRealm();

                    double current = foundationData.getCurrentFoundationProgress();
                    double max = foundationPath.getMaxFoundationProgress(
                            majorRealm,
                            foundationRealm
                    );

                    double progress = max <= 0.0D
                            ? 0.0D
                            : Math.clamp(current / max, 0.0D, 1.0D);

                    return Optional.of(new FoundationState(
                            foundationRealm,
                            progress
                    ));
                })
        );
    }

    private ITextureData getTextureForRealm(int realm) {
        int index = Math.clamp(realm, 0, TIERS.size() - 1);
        return TIERS.get(index);
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        Optional<FoundationState> state = getFoundationState();
        if (state.isEmpty()) {
            return;
        }

        ITextureData texture = getTextureForRealm(state.get().realm());
        int height = (int) Math.round(texture.getHeight() * state.get().progress());

        if (height > 0) {
            texture.render(graphics, texture.getWidth(), height);
        }
    }

    private record FoundationState(
            int realm,
            double progress
    ) {
    }
}