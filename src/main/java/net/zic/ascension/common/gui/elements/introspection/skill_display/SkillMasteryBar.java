package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;

public class SkillMasteryBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );

    private static final ITextureData[] MASTERY_TEXTURES = {
            new TextureDataSubsection(TEXTURE, 234, 286, 193, 198, 7, 65),
            new TextureDataSubsection(TEXTURE, 234, 286, 201, 198, 7, 65),
            new TextureDataSubsection(TEXTURE, 234, 286, 209, 198, 7, 65),
            new TextureDataSubsection(TEXTURE, 234, 286, 217, 198, 7, 65),
            new TextureDataSubsection(TEXTURE, 234, 286, 225, 198, 7, 65)
    };

    private final SkillDisplayContainer owner;
    private final AscensionTooltip tooltip;

    public SkillMasteryBar(UIFrame frame, SkillDisplayContainer owner) {
        super(frame);
        this.owner = owner;
        setWidth(7);
        setHeight(65);
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    private SkillMasteryRank getRank() {
        Identifier skillId = owner.getSelectedSkill();
        if (skillId == null) {
            return null;
        }

        return ClientAscensionData.getSource().map(source -> {
            int progression = SkillProgressionResolver.resolve(source, skillId).effectiveProgression();
            return progression <= 0 ? null : SkillMasteryRank.fromProgression(progression);
        }).orElse(null);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        SkillMasteryRank rank = getRank();
        if (rank == null) {
            return;
        }

        MASTERY_TEXTURES[rank.progression() - 1].render(graphics);

        if (isPointBounded(mouseX, mouseY)) {
            tooltip.setText(Component.translatable(
                    "gui.ascension.introspection.skill_mastery",
                    rank.displayName()
            ));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
