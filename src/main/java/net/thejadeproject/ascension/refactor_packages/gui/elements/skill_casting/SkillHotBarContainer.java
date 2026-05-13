package net.thejadeproject.ascension.refactor_packages.gui.elements.skill_casting;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.SetActiveSlot;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillCastHandler;

public class SkillHotBarContainer extends RenderableElement {
    private int currentIndex = 0;
    public SkillHotBarContainer(UIFrame frame) {
        super(frame);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        setActive(false);
        addEventListener(EasyEvents.FRAME_DIMENSIONS_CHANGE_EVENT,this::rePosition);
    }
    public void rePosition(EasyEvent event){
        getPositioning().updatePositionMatrix();
    }
    public void open(){
        removeChildren();
        Minecraft.getInstance().mouseHandler.releaseMouse();
        setActive(true);
        SkillCastHandler handler = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler();



        double segmentRadius = 360.0/handler.getMaxSlots();
        double currentAngle = 360 -segmentRadius/2 -90;

        for(int i=0;i<handler.getMaxSlots();i++){
            SkillSegment skillSegment = new SkillSegment(getUiFrame(),i,currentAngle,(currentAngle+segmentRadius)%360);

            addChild(skillSegment);
            currentAngle = (currentAngle+segmentRadius)%360;
        }
    }
    public void close(){
        removeChildren();

        setActive(false);
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    @Override
    public void renderTick(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderTick(guiGraphics, mouseX, mouseY, partialTick);
        if(isActive()){
            Vec2 center = new Vec2(getPositioning().getRawX(),getPositioning().getRawY());

            Vec2 change = new Vec2(mouseX-center.x,mouseY-center.y);
            float angle = (float) Math.toDegrees(Math.atan2(change.y, change.x));
            SkillCastHandler handler = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler();



            double segmentRadius = 360.0/handler.getMaxSlots();
            double offset = segmentRadius/2;
            angle += (float) offset + 90f;
            int index = (int) Math.floor((angle < 0 ? 360f+angle : angle)/(segmentRadius));
            if(index != currentIndex){
                currentIndex = index;
                PacketDistributor.sendToServer(new SetActiveSlot(currentIndex));
            }
            for(RenderableElement child : getChildren()) child.setFocused(false);
            getChildren().get(index).setFocused(true);
        }

    }
    //TODO handle the behaviour for which one is hovered
}
