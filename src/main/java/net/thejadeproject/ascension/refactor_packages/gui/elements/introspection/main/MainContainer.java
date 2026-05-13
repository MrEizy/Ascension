package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.main;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.Container;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;

public class MainContainer extends RenderableElement {


    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/menu_gui.png"
    );


    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            256,260,
            0,40,
            234,140
    );
    private final RenderableElement infoContainer;
    public MainContainer(UIFrame frame) {
        super(frame);
        setWidth(bg.getWidth());
        setHeight(bg.getHeight());
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2);
        Container container = new Container(frame,126,67);
        container.getPositioning().setX(94);
        container.getPositioning().setY(51);
        addChild(container);
        infoContainer = container;

        PhysiqueOpenButton physiqueOpenButton = new PhysiqueOpenButton(frame);
        physiqueOpenButton.getPositioning().setX(112);
        physiqueOpenButton.getPositioning().setY(18);
        addChild(physiqueOpenButton);

//        BloodlineOpenButton bloodlineOpenButton = new BloodlineOpenButton(frame);
//        bloodlineOpenButton.getPositioning().setX(112);
//        bloodlineOpenButton.getPositioning().setY(35);
//        addChild(bloodlineOpenButton);


    }

    public void displayPhysique(){
        infoContainer.removeChildren();
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        IPhysique physique = entityData.getPhysique(); //todo add a method to check if they HAVE a physique
        RenderableElement element = physique.getInformationContainer(getUiFrame());
        infoContainer.addChild(element);
        if(element instanceof IInformationContainer informationContainer) informationContainer.refresh();
    }

    public void displayBloodline() {
        infoContainer.removeChildren();

//        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
//        IBloodline bloodline = entityData.getBloodline();
//
//        if (bloodline == null) return;
//
//        RenderableElement element = bloodline.getInformationContainer(getUiFrame());
//        infoContainer.addChild(element);
//
//        if (element instanceof IInformationContainer informationContainer) {
//            informationContainer.refresh();
//        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
