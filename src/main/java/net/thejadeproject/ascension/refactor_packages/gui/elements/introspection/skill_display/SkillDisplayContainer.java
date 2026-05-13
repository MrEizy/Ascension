package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.Container;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.EasyImage;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.BackButton;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;

public class SkillDisplayContainer extends RenderableElement {

    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );


    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            0,40,
            234,157
    );
    ResourceLocation selectedSkill;
    SkillOptionsScrollBox scrollBox;
    PresetHolder presetHolder;
    SkillBarContainer skillBarContainer;
    Container selectedSkillContainer;
    EasyImage selectedSkillIcon;
    public SkillDisplayContainer(UIFrame frame) {
        super(frame);
        setWidth(bg.getWidth());
        setHeight(bg.getHeight());
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2+8);
        BackButton button = new BackButton(frame);
        button.getPositioning().setX(5);
        button.getPositioning().setY(5);
        addChild(button);

        scrollBox = new SkillOptionsScrollBox(frame);
        scrollBox.getPositioning().setX(6);
        scrollBox.getPositioning().setY(43);
        scrollBox.setWidth(89);
        scrollBox.setHeight(91);
        addChild(scrollBox);

        presetHolder = new PresetHolder(frame);
        presetHolder.getPositioning().setX(5);
        presetHolder.getPositioning().setY(141);
        presetHolder.setWidth(90);
        presetHolder.setHeight(11);
        presetHolder.setId("preset_holder");
        addChild(presetHolder);

        skillBarContainer = new SkillBarContainer(frame);
        skillBarContainer.getPositioning().setX(3);
        skillBarContainer.getPositioning().setY(154);
        skillBarContainer.setWidth(95);
        skillBarContainer.setHeight(60);
        skillBarContainer.setId("skill_bar_container");
        skillBarContainer.setActive(false);
        addChild(skillBarContainer);

        selectedSkillContainer = new Container(frame,90,83);

        selectedSkillContainer.getPositioning().setX(121);
        selectedSkillContainer.getPositioning().setY(49);
        addChild(selectedSkillContainer);

        selectedSkillIcon = new EasyImage(getUiFrame(),20);
        selectedSkillIcon.getPositioning().setX(156);
        selectedSkillIcon.getPositioning().setY(19);
        addChild(selectedSkillIcon);


        addChild(new OpenActiveSelection(frame,96,141));


    }
    public void selectSkill(ResourceLocation selectedSkill){
        this.selectedSkill = selectedSkill;
        selectedSkillIcon.setTextureData(null);
        selectedSkillContainer.removeChildren();
        if(selectedSkill != null){
            ISkill skill = AscensionRegistries.Skills.SKILL_REGISTRY.get(selectedSkill);
            selectedSkillIcon.setTextureData(skill.getIcon());
            RenderableElement element = skill.getInformationContainer(getUiFrame());
            selectedSkillContainer.addChild(element);
            if(element instanceof IInformationContainer informationContainer)informationContainer.refresh();
        }
    }
    public ResourceLocation getSelectedSkill(){
        return selectedSkill;
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
