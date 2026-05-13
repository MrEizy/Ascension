package net.zic.ascension.refactor_packages.gui.elements.skill_view.slots;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.EventPhase;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

public class PassiveSkillIcon extends RenderableElement {
    private ResourceLocation skillId;
    private ITextureData skillIcon;
    private Component title;
    private boolean hovered;

    private EasyLabel label;

    private final ITextureData background =  new TextureDataSubsection(
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/screen/skill_stuff/skill_menu.png"),
            320,256,
            0,236,92,20
    );
    public PassiveSkillIcon(UIFrame frame, ResourceLocation skill) {
        super(frame);
        label = new EasyLabel(frame);
        label.getPositioning().setX(27);
        label.getPositioning().setY(5);
        label.setWidth(65);
        label.setHeight(8);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningX(EasyLabel.TextPositionRule.START);
        label.setTextColor(-1);
        label.setTextScale(0.5f);
        setSkill(skill);
        addEventListener(EasyEvents.GLOBAL_MOUSE_MOVE_EVENT,this::onMouseMove, EventPhase.BUBBLE);
        addChild(label);
        setWidth(background.getWidth());
        setHeight(background.getHeight());
        this.skillId = skill;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        background.renderAt(guiGraphics,2,0,background.getWidth(),background.getHeight());
        if(skillIcon != null){
            skillIcon.renderAt(guiGraphics,4,2);
        }
        if(isHovered()) guiGraphics.fill(0,0,getWidth(),getHeight(),-930773627);
    }

    public void setSkill(ResourceLocation skill){
        this.skillId = skill;
        skillIcon = skill != null ? AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getIcon() : null;
        Component text = skill != null ? AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getTitle() : Component.empty();
        if(text == null) text = Component.empty();
        label.setText(text);
    }
    public boolean isHovered(){return hovered;}
    public void onMouseMove(EasyEvent event){
        hovered = event.getTarget() == this;
    }
}
