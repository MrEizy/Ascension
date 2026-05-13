package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.EasyTooltip;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.paths.IPath;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.text.DecimalFormat;

public class PathProgressBar extends RenderableElement {
    ResourceLocation selectedPath;
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );
    ITextureData progressTexture = new TextureDataSubsection(
            textureIdentifier,
            234,216,
            1,181,
            84,9
    );
    DecimalFormat format = new DecimalFormat("0.##");
    EasyTooltip tooltip;
    boolean hovered;
    public PathProgressBar(UIFrame frame, ResourceLocation path) {
        super(frame);
        this.selectedPath = path;
        setWidth(progressTexture.getWidth());
        setHeight(progressTexture.getHeight());
        tooltip = new EasyTooltip(frame);
        tooltip.setActive(true);
        addEventListener(EasyEvents.MOUSE_MOVE_EVENT,this::onMouseMove);
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT,this::onMouseMoveGlobal);
    }
    public void onMouseMoveGlobal(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;

        if(hovered && !isPointBounded(easyMouseEvent.getMouseX(),easyMouseEvent.getMouseY())) {
            hovered=false;
            if(getUiFrame().getTooltip() == tooltip) getUiFrame().setTooltip(null);
        }
    }
    public void onMouseMove(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;
        hovered = true;
    }

    public void setPath(ResourceLocation path){
        this.selectedPath = path;
    }
    public double getMaxQi(){
        if(selectedPath == null) return 0;
        PathData pathData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getPathData(selectedPath);

        double maxQi;
        if(pathData.getLastUsedTechnique() == null){
            maxQi = AscensionRegistries.Paths.PATHS_REGISTRY.get(selectedPath).getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm());
        }else maxQi = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique()).getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm());
        return maxQi;
    }
    public double getCurrentQi(){
        if(selectedPath == null) return 0;
        PathData pathData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getPathData(selectedPath);
        return pathData.getCurrentRealmProgress();
    }
    public double getProgress(){
        if(selectedPath == null) return 0;

        return Math.clamp(getCurrentQi()/getMaxQi(),0,1);

    }

    @Override
    protected void run(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.run(guiGraphics, mouseX, mouseY, partialTick);
        if(hovered){
            //System.out.println("show progress for path : "+selectedPath);
            tooltip = new EasyTooltip(getUiFrame());
            tooltip.appendText(Component.literal(format.format(getCurrentQi())).append("/").append(format.format(getMaxQi())));
            getUiFrame().setTooltip(tooltip);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        progressTexture.render(guiGraphics, (int) (progressTexture.getWidth()*getProgress()),progressTexture.getHeight());
    }
}
