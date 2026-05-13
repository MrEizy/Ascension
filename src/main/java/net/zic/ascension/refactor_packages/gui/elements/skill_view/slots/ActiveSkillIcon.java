package net.zic.ascension.refactor_packages.gui.elements.skill_view.slots;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.EventPhase;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.gui.elements.skill_view.SkillMenuContainer;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

public class ActiveSkillIcon extends RenderableElement {

    private ResourceLocation skillId;
    private ITextureData skillIcon;
    private boolean hovered;
    public ActiveSkillIcon(UIFrame frame) {
        super(frame);
        setWidth(18);
        setHeight(18);
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT,this::onMouseMove, EventPhase.BUBBLE);
        addEventListener(EasyEvents.MOUSE_DOWN_EVENT,this::onMouseDown,EventPhase.BUBBLE);
    }
    public void onMouseDown(EasyEvent event){
        if(event.getTarget() != this) return;

        if(getUiFrame().getElementById("container") instanceof SkillMenuContainer container){
            container.setHeldSkill(skillId);
        }
    }
    public void setSkill(ResourceLocation skill){
        this.skillId = skill;
        skillIcon = skill != null ? AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getIcon() : null;
    }
    public ResourceLocation getSkill(){
        return skillId;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(skillIcon != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(1,1,0);
            skillIcon.render(guiGraphics);
            guiGraphics.pose().popPose();
        };
        if(isHovered()) guiGraphics.fill(1,1,17,17,-930773627);
    }
    public boolean isHovered(){return hovered;}
    public void onMouseMove(EasyEvent event){
        hovered = event.getTarget() == this;
    }
}
