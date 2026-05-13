package net.zic.ascension.refactor_packages.gui.elements.skill_view;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.gui.elements.general.ScrollBox;
import net.zic.ascension.refactor_packages.gui.elements.skill_view.slots.PassiveSkillIcon;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;
import net.zic.ascension.refactor_packages.skills.castable.ICastableSkill;

public class PassiveSkillList  extends RenderableElement {
    ScrollBox scrollBox;
    private final ITextureData background =  new TextureDataSubsection(
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/screen/skill_stuff/skill_menu.png"),
            320,256,
            198,0,122,208
    );

    public PassiveSkillList(UIFrame frame) {
        super(frame);
        setWidth(background.getWidth());
        setHeight(background.getHeight());

        this.scrollBox = new ScrollBox(frame,22);
        this.scrollBox.setWidth(98);
        this.scrollBox.setHeight(180);
        scrollBox.getPositioning().setY(16);
        scrollBox.getPositioning().setX(12);

        refreshSkills();
        addChild(scrollBox);
    }

    public void refreshSkills(){
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        for(ResourceLocation skillId : entityData.getAllSkills()){
            if(AscensionRegistries.Skills.SKILL_REGISTRY.get(skillId) instanceof ICastableSkill skill) continue;
            //only do passive skills
            scrollBox.addChild(new PassiveSkillIcon(getUiFrame(),skillId));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        background.render(guiGraphics);
    }
}
