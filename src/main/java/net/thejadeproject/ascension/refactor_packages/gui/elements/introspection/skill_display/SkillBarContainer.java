package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display.skill_buttons.SkillSelectionButton;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.ClearSlot;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.SetActiveSlot;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.UpdateSkillSlot;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillHotBar;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;

import java.util.List;

public class SkillBarContainer extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );


    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            234,286,
            3,198,
            95,60
    );
    public SkillBarContainer(UIFrame frame) {
        super(frame);
        addEventListener(EasyEvents.MOUSE_UP_EVENT,this::onMouseUp);
    }
    public void onMouseUp(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;
        if(event.getTarget() == this) return;

        SkillSelectionButton btn = null;
        if(event.getTarget() instanceof SkillSelectionButton button) btn = button;
        if(event.getTarget().getParent() instanceof SkillSelectionButton button2) btn = button2;
        if(btn == null) return;
        SkillDisplayContainer parentContainer = (SkillDisplayContainer) getParent();

        ResourceLocation skill = btn.getSkill();
        int slot = getChildren().indexOf(btn);
        boolean shouldClearSlot = parentContainer.getSelectedSkill() == null
                || parentContainer.getSelectedSkill().equals(skill)
                || !(AscensionRegistries.Skills.SKILL_REGISTRY.get(parentContainer.getSelectedSkill()) instanceof ICastableSkill);
        if(!shouldClearSlot) updateOtherSlots(parentContainer.getSelectedSkill());
        btn.setSkill(shouldClearSlot ? null : parentContainer.getSelectedSkill());


        if(!shouldClearSlot){
            PacketDistributor.sendToServer(new UpdateSkillSlot(slot,parentContainer.getSelectedSkill().toString()));

        }
        else PacketDistributor.sendToServer(new ClearSlot(slot));
    }
    public void updateOtherSlots(ResourceLocation skill){
        List<RenderableElement> elements = getChildren();
        for(RenderableElement element : elements){
            if(element instanceof SkillSelectionButton btn){

                if(btn.getSkill() != null && btn.getSkill().equals(skill)) btn.setSkill(null);
            }
        }
    }
    public void open(){
        setActive(true);
        SkillHotBar hotBar = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().getHotBar();

        for(int i = 0; i<hotBar.MAX_SLOTS;i++){
            SkillSelectionButton btn = new SkillSelectionButton(getUiFrame(),hotBar.getSkillKey(i));
            btn.getPositioning().setX(2);
            btn.getPositioning().setY(i*btn.getHeight());
            addChild(btn);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if(!isActive()) removeChildren();
    }
}
