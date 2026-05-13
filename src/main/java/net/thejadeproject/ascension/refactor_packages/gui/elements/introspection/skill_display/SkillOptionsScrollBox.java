package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.ScrollBox;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display.skill_buttons.SkillSelectionButton;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.ClearSlot;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.UpdateSkillSlot;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;

import java.util.Collection;

public class SkillOptionsScrollBox extends ScrollBox {
    public SkillOptionsScrollBox(UIFrame frame) {
        super(frame, 5);
        setWidth(89);
        setHeight(91);
        loadSkills();
        addEventListener(EasyEvents.MOUSE_UP_EVENT,this::onMouseUp);
    }
    public void onMouseUp(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;
        //System.out.println("mouse up");
        if(event.getTarget() == this) return;
        SkillSelectionButton btn = null;
        if(event.getTarget() instanceof SkillSelectionButton button) btn = button;
        if(event.getTarget().getParent() instanceof SkillSelectionButton button2) btn = button2;
        if(btn == null) return;
        SkillDisplayContainer parentContainer = (SkillDisplayContainer) getParent();

        ResourceLocation skill = btn.getSkill();
        parentContainer.selectSkill(skill);
    }

    @Override
    public int getMaxYScroll() {
        return Math.max(0,(getChildren().size()+2)*12-getHeight());
    }

    @Override
    public void updateVisibility(RenderableElement element) {
        boolean condition =
                !(element.getPositioning().getY()+element.getHeight() <= 0) &&
                        !(element.getPositioning().getY() > getHeight());
        element.setVisible(condition);
    }

    public void loadSkills(){
        IEntityData entityData = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA);
        Collection<ResourceLocation> skills =  entityData.getAllSkills();
        for(ResourceLocation skill : skills){
            SkillSelectionButton btn = new SkillSelectionButton(getUiFrame(),skill);
            addChild(btn);
        }
    }
    @Override
    public void updatePos(RenderableElement element) {
        if(getChildren().isEmpty()) return;
        element.getPositioning().setFromRawY(getChildren().getLast().getPositioning().getRawY()+getChildren().getLast().getHeight()+2);
    }
}