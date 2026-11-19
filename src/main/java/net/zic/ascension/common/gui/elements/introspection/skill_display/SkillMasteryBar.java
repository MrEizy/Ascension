package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionSnapshot;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;

import java.util.Locale;

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

    private MasteryView getMastery() {
        Identifier skillId = owner.getSelectedSkill();
        if (skillId == null) {
            return null;
        }

        return ClientAscensionData.getSource().map(source -> {
            SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
            if (instance == null || !(instance.getSkill() instanceof ActiveSkill skill)) {
                return null;
            }
            SkillProgressionSnapshot snapshot = SkillProgressionResolver.resolve(source, skillId);
            return snapshot.permanentProgression() < 1 ? null : new MasteryView(skill, snapshot);
        }).orElse(null);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        MasteryView mastery = getMastery();
        if (mastery == null) {
            return;
        }

        SkillProgressionSnapshot state = mastery.snapshot();
        SkillMasteryRank rank = SkillMasteryRank.fromProgression(state.permanentProgression());
        MASTERY_TEXTURES[rank.progression() - 1].render(graphics);

        Component description;
        if (state.trainedProgression() >= SkillMasteryRank.TRANSCENDENCE.progression()) {
            description = Component.translatable("gui.ascension.introspection.skill_mastery_maxed", rank.displayName());
        } else if (state.trainedProgression() >= state.accessibleCap()) {
            description = Component.translatable(
                    "gui.ascension.introspection.skill_mastery_capped",
                    rank.displayName(),
                    SkillMasteryRank.fromProgression(state.accessibleCap()).displayName()
            );
        } else {
            double required = mastery.skill().getExperienceRequiredForNextProgression(state.trainedProgression());
            if (!Double.isFinite(required) || required <= 0.0D) {
                description = Component.translatable("gui.ascension.introspection.skill_mastery_unavailable", rank.displayName());
            } else {
                double fraction = Math.clamp(state.experience() / required, 0.0D, 1.0D);
                int filled = (int) Math.round(getHeight() * fraction);
                if (filled > 0) {
                    graphics.enableScissor(0, getHeight() - filled, getWidth(), getHeight());
                    MASTERY_TEXTURES[rank.progression()].render(graphics);
                    graphics.disableScissor();
                }
                description = Component.translatable(
                        "gui.ascension.introspection.skill_mastery_progress",
                        rank.displayName(),
                        formatExperience(state.experience()),
                        formatExperience(required)
                );
            }
        }

        if (isPointBounded(mouseX, mouseY)) {
            tooltip.setText(description);
            getUiFrame().setTooltip(tooltip);
        }
    }

    private static String formatExperience(double value) {
        return String.format(Locale.ROOT, "%,.0f", value);
    }

    private record MasteryView(ActiveSkill skill, SkillProgressionSnapshot snapshot) {
    }
}
