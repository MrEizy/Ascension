package net.thejadeproject.ascension.refactor_packages.gui.elements.skills.cultivation;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

public class CultivationProgressBar extends RenderableElement {
    ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/overlay/overlays_all.png");
    ITextureData barBackground = new TextureDataSubsection(
            textureLocation,
            256,256,
            0,21,
            72,28
    );
    ITextureData barDetail = new TextureDataSubsection(
            textureLocation,
            256,256,
            67,0,
            135,21
    );
    ITextureData barContent;
    ResourceLocation path;
    public CultivationProgressBar(UIFrame frame,ITextureData barContent,ResourceLocation path) {
        super(frame);
        this.barContent = barContent;
        getPositioning().setYPositioningRule(PositioningRules.END);
        getPositioning().setXPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-barBackground.getWidth()/2);
        getPositioning().setY(50);
        this.path = path;
    }

    double getProgress(){
        PathData pathData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getPathData(path);
        if(pathData != null && pathData.getLastUsedTechnique() == null) return 0;
        if(pathData == null || pathData.getLastUsedTechnique() == null)return 0.0;
        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());

        return pathData.getCurrentRealmProgress()/technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        barBackground.render(guiGraphics);
        barContent.renderAt(guiGraphics,1,1, (int) ((barContent.getWidth())*getProgress()),barContent.getHeight());
        barDetail.renderAt(guiGraphics,4,-10);
    }
}
