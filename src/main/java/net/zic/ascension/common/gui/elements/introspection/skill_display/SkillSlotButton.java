package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.BetterButton;

public class SkillSlotButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_text_buttons.png"
    );

    private final SkillDisplayContainer owner;
    private final int slot;
    private final EasyLabel label;
    private final ITextureData alternateTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 0, 89, 12
    );
    private final ITextureData defaultTexture = new TextureDataSubsection(
            TEXTURE, 89, 24, 0, 12, 89, 12
    );

    private Identifier displayedSkill;
    private boolean selected;

    public SkillSlotButton(UIFrame frame, SkillDisplayContainer owner, int slot) {
        super(frame, 0, slot * 10);
        this.owner = owner;
        this.slot = slot;
        setWidth(defaultTexture.getWidth());
        setHeight(defaultTexture.getHeight());
        getTransform().setScale(0.75F);

        label = new EasyLabel(frame);
        label.setText(Component.empty());
        label.setTextColor(0xFFFFFFFF);
        label.setWidth(85);
        label.setHeight(8);
        label.getPositioning().setX(2);
        label.getPositioning().setY(2);
        label.setScaleToFit(true);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setTextPositioningY(EasyLabel.TextPositionRule.CENTER);
        addChild(label);
        refreshState();
    }

    public int getSlot() {
        return slot;
    }

    public Identifier getDisplayedSkill() {
        return displayedSkill;
    }

    private void refreshState() {
        ClientAscensionData.getSkillCastHandler().ifPresentOrElse(handler -> {
            Identifier skill = handler.getSkill(slot);
            boolean selectedNow = handler.getSelectedSlot() == slot;
            if (java.util.Objects.equals(skill, displayedSkill) && selectedNow == selected) {
                return;
            }

            displayedSkill = skill;
            selected = selectedNow;
            label.setText(createLabel(skill));
            label.setTextScale(1.0F);
        }, () -> {
            displayedSkill = null;
            selected = false;
            label.setText(createLabel(null));
            label.setTextScale(1.0F);
        });
    }

    private Component createLabel(Identifier skillId) {
        if (skillId == null) {
            return Component.translatable(
                    "gui.ascension.introspection.empty_slot"
            );
        }

        return ClientAscensionData.getPlayer()
                .map(player -> {
                    Skill skill = CoreRegistries.safeAccess(
                            CoreRegistries.SKILL_REGISTRY,
                            skillId,
                            player.registryAccess()
                    );

                    return skill == null
                            ? Component.literal(skillId.toString())
                            : skill.getName();
                })
                .orElseGet(() -> Component.literal(skillId.toString()));
    }

    @Override
    public void onClick() {
        owner.handleSlotClick(this);
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        refreshState();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (selected || isHovered() || isPressed()) {
            alternateTexture.render(graphics);
        } else {
            defaultTexture.render(graphics);
        }
    }
}
