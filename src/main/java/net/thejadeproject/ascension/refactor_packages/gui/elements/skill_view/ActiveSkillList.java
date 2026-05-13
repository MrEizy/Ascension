package net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollBox;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.slots.ActiveSkillIcon;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.slots.ActiveSkillSlot;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;

public class ActiveSkillList extends RenderableElement {
    private ScrollBox scrollBox;
    private final ITextureData background = new TextureDataSubsection(
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/screen/skill_stuff/skill_menu.png"),
            320,256,
            0,89,
            192,119
    );

    public ActiveSkillList(UIFrame frame) {
        super(frame);
        setHeight(background.getHeight());
        setWidth(background.getWidth());
        ScrollBox scrollBox = new ScrollBox(frame,18);
        scrollBox.getPositioning().setY(32);
        scrollBox.getPositioning().setX(15);
        scrollBox.setWidth(81);
        scrollBox.setHeight(72);
        this.scrollBox = scrollBox;
        addChild(scrollBox);

        refreshSkills();
    }

    public void refreshSkills(){
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        for(ResourceLocation skillId : entityData.getAllSkills()){
            if(!(AscensionRegistries.Skills.SKILL_REGISTRY.get(skillId) instanceof ICastableSkill skill)) continue;
            //only do active skills
            ActiveSkillIcon skillIcon = new ActiveSkillIcon(getUiFrame());
            //System.out.println("trying to add skill :" +skillId.toString());
            skillIcon.setSkill(skillId);
            scrollBox.addChild(skillIcon);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        background.render(guiGraphics);
    }

}
