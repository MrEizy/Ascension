package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.path_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.Container;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.BackButton;
import net.thejadeproject.ascension.refactor_packages.paths.IPath;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

import java.util.Collection;
import java.util.List;

public class PathDisplayContainer extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/path_menu/path_menu.png"
    );


    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            234,216,
            0,39,
            234,140
    );
    PathOptionsScrollBox displayContainer;
    Container pathContainer;
    EasyLabel selectedTechniqueLabel;
    PathProgressBar pathProgressBar;
    public PathDisplayContainer(UIFrame frame) {
        super(frame);
        setWidth(bg.getWidth());
        setHeight(bg.getHeight());
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2);
        displayContainer = new PathOptionsScrollBox(frame);
        displayContainer.getPositioning().setX(6);
        displayContainer.getPositioning().setY(43);

        addChild(displayContainer);
        addPaths();

        selectedTechniqueLabel = new EasyLabel(frame);
        selectedTechniqueLabel.setText(Component.literal("none"));
        selectedTechniqueLabel.setTextColor(-1);
        selectedTechniqueLabel.setScaleToFit(true);
        selectedTechniqueLabel.setWidth(78);
        selectedTechniqueLabel.setHeight(8);
        selectedTechniqueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        selectedTechniqueLabel.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        selectedTechniqueLabel.getPositioning().setX(131);
        selectedTechniqueLabel.getPositioning().setY(16);

        addChild(selectedTechniqueLabel);

        pathContainer = new Container(frame,94,85);

        pathContainer.getPositioning().setX(123);
        pathContainer.getPositioning().setY(46);
        addChild(pathContainer);

        pathProgressBar = new PathProgressBar(frame,null);

        pathProgressBar.getPositioning().setX(128);
        pathProgressBar.getPositioning().setY(31);
        addChild(pathProgressBar);

        BackButton button = new BackButton(frame);
        button.getPositioning().setX(5);
        button.getPositioning().setY(5);
        addChild(button);
        //TODO add description box
    }
    public void selectPath(ResourceLocation path){
        PathData pathData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getPathData(path);
        pathContainer.removeChildren();
        selectedTechniqueLabel.setText(Component.literal("none"));
        pathProgressBar.setPath(path);
        if(pathData.getLastUsedTechnique() == null){
            IPath pathInstance = AscensionRegistries.Paths.PATHS_REGISTRY.get(path);

            RenderableElement element = pathInstance.getInformationContainer(getUiFrame(),pathData);
            pathContainer.addChild(element);
            if(element instanceof IInformationContainer informationContainer)informationContainer.refresh();
        }else{
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
            selectedTechniqueLabel.setText(technique.getDisplayTitle());
            RenderableElement element = technique.getInformationContainer(getUiFrame(),pathData);
            pathContainer.addChild(element);
            if(element instanceof IInformationContainer informationContainer)informationContainer.refresh();
        }
    }
    public void addPaths(){
        //TODO handle filter from text box
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        Collection<PathData> pathData =  entityData.getAllPathData();
        for(PathData path : pathData){
            PathSelectionButton selectionButton = new PathSelectionButton(getUiFrame(),path.getPath()){
                @Override
                public void onClick() {
                    super.onClick();
                    selectPath(this.path);
                }
            };
            displayContainer.addChild(selectionButton);
        }
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
