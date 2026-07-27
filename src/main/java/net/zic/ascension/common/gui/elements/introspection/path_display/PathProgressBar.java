package net.zic.ascension.common.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.common.gui.data.ClientAscensionData;

public class PathProgressBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );

    private static final ITextureData PROGRESS_TEXTURE = new TextureDataSubsection(
            TEXTURE, 234, 287, 1, 181, 84, 9
    );

    private Identifier selectedPath;

    public PathProgressBar(UIFrame frame) {
        super(frame);
        setWidth(PROGRESS_TEXTURE.getWidth());
        setHeight(PROGRESS_TEXTURE.getHeight());
    }

    public void setPath(Identifier pathId) {
        selectedPath = pathId;
    }

    private double getProgress() {
        if (selectedPath == null) {
            return 0.0D;
        }

        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSource().map(source -> {
                    PathData pathData = AscensionOriginSourceHelper.getPathData(source,selectedPath);
                    if (pathData == null) {
                        return 0.0D;
                    }
                    double maximum = pathData.getMaxProgress(
                            pathData.getMajorRealm(),
                            pathData.getMinorRealm(),
                            player.registryAccess()
                    );
                    if (maximum <= 0.0D) {
                        return 0.0D;
                    }
                    return Math.clamp(pathData.getProgress() / maximum, 0.0D, 1.0D);
                })
        ).orElse(0.0D);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int width = (int) Math.round(PROGRESS_TEXTURE.getWidth() * getProgress());
        if (width > 0) {
            PROGRESS_TEXTURE.render(graphics, width, PROGRESS_TEXTURE.getHeight());
        }
    }
}
