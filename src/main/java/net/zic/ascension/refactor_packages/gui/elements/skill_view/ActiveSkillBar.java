package net.zic.ascension.refactor_packages.gui.elements.skill_view;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.refactor_packages.gui.elements.general.Container;
import net.zic.ascension.refactor_packages.gui.elements.skill_view.slots.ActiveSkillSlot;

public class ActiveSkillBar extends RenderableElement {
    ITextureData background = new TextureDataSubsection(
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/screen/skill_stuff/skill_menu.png"),
            320,256,
            0,0,
            192,83
    );
    Container container;
    public ActiveSkillBar(UIFrame frame) {
        super(frame);
        setHeight(background.getHeight());
        setWidth(background.getWidth());
        container = new Container(frame,162,36);
        for(int i=0;i<18;i++){
            ActiveSkillSlot slot = new ActiveSkillSlot(frame,i);
            slot.getPositioning().setX(i%9 * 18);
            slot.getPositioning().setY(i<9?0:18);
            container.addChild(slot);
        }
        container.getPositioning().setY(32);
        container.getPositioning().setX(15);
        addChild(container);
    }
    public void removeSkill(ResourceLocation skill){
        if(skill == null) return;
        for(RenderableElement child : container.getChildren()){
            if(child instanceof ActiveSkillSlot slot){
                if(skill.equals(slot.getSkill())){
                    slot.setSkillNoPacket(null);
                }
            }
        }
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        background.render(guiGraphics);
    }
}
