package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;

import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.network.TogglePassiveSkillPacket;

public class TogglePassiveButton extends BetterButton {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );

    private static final ITextureData CHECKMARK = new TextureDataSubsection(
            TEXTURE,
            234, 286,
            191, 232,
            9, 7
    );

    private final SkillDisplayContainer owner;
    private final AscensionTooltip tooltip;

    private boolean toggleable;
    private boolean enabled;

    public TogglePassiveButton(
            UIFrame frame,
            SkillDisplayContainer owner,
            int x,
            int y
    ) {
        super(frame, x, y);
        this.owner = owner;
        setWidth(13);
        setHeight(13);

        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
        refreshState();
    }

    @Override
    public void onClick() {
        Identifier skillId = owner.getSelectedSkill();
        if (!toggleable || skillId == null) {
            return;
        }

        ClientPacketDistributor.sendToServer(
                new TogglePassiveSkillPacket(skillId, !enabled)
        );
    }

    private void refreshState() {
        toggleable = false;
        enabled = false;

        Identifier skillId = owner.getSelectedSkill();
        if (skillId == null) {
            return;
        }

        ClientAscensionData.getPlayer().ifPresent(player ->
                ClientAscensionData.getSource().ifPresent(source ->
                        updateFromSource(player.registryAccess(), source, skillId)
                )
        );
    }

    private void updateFromSource(
            net.minecraft.core.RegistryAccess registryAccess,
            OriginSource source,
            Identifier skillId
    ) {
        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                registryAccess
        );
        SkillData data = AscensionOriginSourceHelper.getSkillData(source,skillId);

        if (skill instanceof ToggleableSkill toggleableSkill && data != null) {
            toggleable = true;
            enabled = toggleableSkill.isEnabled(data);
        }
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
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (enabled) {
            CHECKMARK.renderAt(graphics, 2, 3);
        }

        if (!toggleable) {
            graphics.fill(0, 0, getWidth(), getHeight(), 0x64999999);
            return;
        }

        if (isHovered()) {
            graphics.fill(0, 0, getWidth(), getHeight(), 0x32999999);
            tooltip.setText(Component.translatable(
                    enabled
                            ? "gui.ascension.introspection.toggle_passive.disable"
                            : "gui.ascension.introspection.toggle_passive.enable"
            ));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
