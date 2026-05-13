package net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.slots;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.EventPhase;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.ActiveSkillBar;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.SkillMenuContainer;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.ClearSlot;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.UpdateSkillSlot;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillCastHandler;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;

public class ActiveSkillSlot extends RenderableElement {
    private boolean enabled;
    private int slot;
    private ResourceLocation skill;
    private ITextureData icon;
    private boolean hovered;

    public ActiveSkillSlot(UIFrame frame,int slot) {
        super(frame);
        setWidth(18);
        setHeight(18);
        this.slot = slot;
        refresh();
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT,this::onMouseMove, EventPhase.BUBBLE);
        addEventListener(EasyEvents.MOUSE_DOWN_EVENT,this::onMouseDown,EventPhase.BUBBLE);
        addEventListener(EasyEvents.MOUSE_UP_EVENT,this::onMouseUp,EventPhase.BUBBLE);
    }
    public void onMouseUp(EasyEvent event){
        if(event.getTarget() != this)return;

        if(getUiFrame().getElementById("container") instanceof SkillMenuContainer container && container.getHeldSkill() != null){
            setSkill(container.getHeldSkill());
            container.setHeldSkill(null);
        }

    }
    public void onMouseDown(EasyEvent event){
        if(event.getTarget() != this) return;

        if(getUiFrame().getElementById("container") instanceof SkillMenuContainer container){
            container.setHeldSkill(skill);
        }
    }
    public void refresh(){
        SkillCastHandler handler = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler();
        int maxSlots = handler.getMaxSlots();
        enabled = slot<maxSlots;
        skill = null;
        icon = null;
        if(enabled && handler.getHotBar().getSkillKey(slot) != null){
            skill = handler.getHotBar().getSkillKey(slot);
            icon = handler.getHotBar().getSkill(slot).getIcon();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if(enabled){
            //TODO replace with renderAt
            if(icon != null) icon.renderAt(guiGraphics,1,1);
            if(isHovered()) guiGraphics.fill(1,1,17,17,-930773627);
        }else{

            guiGraphics.fill(1,1,17,17,-14209995);
        }
    }
    public boolean isHovered(){return hovered;}
    public void onMouseMove(EasyEvent event){
        hovered = enabled && event.getTarget() == this;
    }
    public void setSkillNoPacket(ResourceLocation skill){
        if(!enabled) return;
        this.skill = skill;
        this.icon = null;
        if(skill != null){
            icon = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getIcon();
        }
    }
    public void setSkill(ResourceLocation skill){
        if(!enabled) return;
        //System.out.println("trying to add "+skill+" to slot "+slot);
        //TODO send update packet then update self
        if(skill == null){
            PacketDistributor.sendToServer(new ClearSlot(slot));
        }else{


            if(AscensionRegistries.Skills.SKILL_REGISTRY.get(skill) instanceof ICastableSkill castableSkill){
                PacketDistributor.sendToServer(new UpdateSkillSlot(slot,skill.toString()));
                ((ActiveSkillBar)getParent().getParent()).removeSkill(skill);
                this.skill = skill;
                icon = castableSkill.getIcon();


            }

        }

    }
    public ResourceLocation getSkill(){
        return skill;
    }
}
