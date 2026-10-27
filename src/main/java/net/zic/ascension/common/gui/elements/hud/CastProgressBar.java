package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.impl.core.skill.castable.GoldenCoreFormingSkill;

import java.util.Optional;

/**
 * shows the cast progress of a golden core forming skill, in the same spot and style as the {@link CultivationProgressBar}.
 * the two never show at once, the cultivation bar only shows for cultivation skills
 */
public class CastProgressBar extends RenderableElement {

    public CastProgressBar(UIFrame frame) {
        super(frame);

        setWidth(CultivationProgressBar.FRAME_WIDTH);
        setHeight(CultivationProgressBar.FRAME_HEIGHT);

        getPositioning().setXPositioningRule(PositioningRules.CENTER);
        getPositioning().setYPositioningRule(PositioningRules.END);
        getPositioning().setX(-CultivationProgressBar.FRAME_WIDTH / 2);
        getPositioning().setY(86);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && HudVisibility.isHudVisible();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Optional<CastState> optionalState = getState();
        if (optionalState.isEmpty()) {
            return;
        }

        CastState state = optionalState.get();

        CultivationProgressBar.BACKGROUND.render(graphics);

        int filledWidth = (int) Math.round(CultivationProgressBar.CONTENT_WIDTH * state.progress());
        filledWidth = Math.clamp(filledWidth, 0, CultivationProgressBar.CONTENT_WIDTH);

        if (filledWidth > 0) {
            state.content().renderAt(
                    graphics,
                    CultivationProgressBar.CONTENT_X,
                    CultivationProgressBar.CONTENT_Y,
                    filledWidth,
                    CultivationProgressBar.CONTENT_HEIGHT
            );
        }
    }

    //the local player's forming cast, coloured by the path the core is formed on
    private Optional<CastState> getState() {
        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSkillCastHandler().flatMap(handler -> {
                    Identifier castingSkillId = handler.getCastingSkill();
                    if (castingSkillId == null) {
                        return Optional.empty();
                    }

                    Skill<?> skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, castingSkillId, player.registryAccess());
                    if (!(skill instanceof GoldenCoreFormingSkill formingSkill)) {
                        return Optional.empty();
                    }

                    ActiveCastVisualState visualState = handler.getActiveCastVisualState();
                    if (visualState == null) {
                        return Optional.empty();
                    }

                    return Optional.of(new CastState(visualState.progress(), CultivationProgressBar.contentFor(formingSkill.path())));
                })
        );
    }

    private record CastState(double progress, ITextureData content) {
    }
}
