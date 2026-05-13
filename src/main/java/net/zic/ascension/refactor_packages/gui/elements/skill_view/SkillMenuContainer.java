package net.zic.ascension.refactor_packages.gui.elements.skill_view;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

public class SkillMenuContainer extends RenderableElement {

    private ResourceLocation heldSkill;
    private ITextureData heldSkillIcon;
    private boolean scheduledHeldSKillRemoval;
    public SkillMenuContainer(UIFrame frame) {
        super(frame);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        setId("container");
        RenderableElement activeContainer = new ActiveSkillList(frame);
        RenderableElement activeSkillContainer = new ActiveSkillBar(frame);
        RenderableElement passiveContainer = new PassiveSkillList(frame);

        passiveContainer.getPositioning().setX(38);
        passiveContainer.getPositioning().setY(-passiveContainer.getHeight()/2);

        activeSkillContainer.getPositioning().setX(-160);
        activeSkillContainer.getPositioning().setY(-passiveContainer.getHeight()/2);

        activeContainer.getPositioning().setX(-160);
        activeContainer.getPositioning().setY(activeSkillContainer.getPositioning().getRawY() +6+activeSkillContainer.getHeight());
        addChild(activeContainer);
        addChild(activeSkillContainer);
        addChild(passiveContainer);
        addEventListener(EasyEvents.GLOBAL_MOUSE_UP_EVENT,event -> {
            if(this.heldSkill != null) scheduledHeldSKillRemoval = true;
        });

    }

    @Override
    public void runChildren(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.runChildren(guiGraphics, mouseX, mouseY, partialTick);
        if(heldSkillIcon != null){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0,0,1000);

            Vec2 global=getGlobalPoint();

            heldSkillIcon.renderAt(guiGraphics, (int) (mouseX-global.x-heldSkillIcon.getWidth()/2), (int) (mouseY- global.y-heldSkillIcon.getWidth()/2));
            guiGraphics.pose().popPose();
        }
    }

    public void setHeldSkill(ResourceLocation skill){
        this.heldSkill = skill;
        this.heldSkillIcon = skill == null? null : AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getIcon();
    }
    public ResourceLocation getHeldSkill(){
        return heldSkill;
    }

    @Override
    public void renderTick(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(scheduledHeldSKillRemoval){
            setHeldSkill(null);
            scheduledHeldSKillRemoval = false;
        }
        super.renderTick(guiGraphics, mouseX, mouseY, partialTick);

    }
}
