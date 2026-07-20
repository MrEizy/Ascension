package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;

import java.util.Optional;

public class CultivationProgressBar extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/overlay/overlays_all.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 256, 256, 0, 21, 72, 28
    );
    private static final ITextureData DETAIL = new TextureDataSubsection(
            TEXTURE, 256, 256, 67, 0, 135, 21
    );
    private static final ITextureData ESSENCE_CONTENT = new TextureDataSubsection(
            TEXTURE, 256, 256, 0, 0, 67, 5
    );
    private static final ITextureData SOUL_CONTENT = new TextureDataSubsection(
            TEXTURE, 256, 256, 0, 6, 67, 5
    );
    private static final ITextureData BODY_CONTENT = new TextureDataSubsection(
            TEXTURE, 256, 256, 0, 12, 67, 5
    );

    public CultivationProgressBar(UIFrame frame) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setXPositioningRule(PositioningRules.CENTER);
        getPositioning().setYPositioningRule(PositioningRules.END);
        getPositioning().setX(-BACKGROUND.getWidth() / 2);
        getPositioning().setY(50);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Optional<CultivationState> optionalState = getState();
        if (optionalState.isEmpty()) {
            return;
        }

        CultivationState state = optionalState.get();
        BACKGROUND.render(graphics);

        int width = (int) Math.round(state.content().getWidth() * state.progress());
        if (width > 0) {
            state.content().renderAt(graphics, 1, 1, width, state.content().getHeight());
        }

        DETAIL.renderAt(graphics, 4, -10);
    }

    private Optional<CultivationState> getState() {
        return ClientAscensionData.getPlayer().flatMap(player ->
                ClientAscensionData.getSkillCastHandler().flatMap(handler -> {
                    Identifier castingSkillId = handler.getCastingSkill();
                    if (castingSkillId == null) {
                        return Optional.empty();
                    }

                    Skill skill = CoreRegistries.safeAccess(
                            CoreRegistries.SKILL_REGISTRY,
                            castingSkillId,
                            player.registryAccess()
                    );
                    if (!(skill instanceof SimpleCultivationSkill cultivationSkill)) {
                        return Optional.empty();
                    }

                    return ClientAscensionData.getEntityData().flatMap(entityData -> {
                        PathData pathData = entityData.getSource().getPathData(cultivationSkill.primaryPath());
                        if (pathData == null) {
                            return Optional.empty();
                        }

                        double progress = resolveProgress(
                                pathData,
                                cultivationSkill.primaryPath(),
                                entityData.isCultivationSuppressed(),
                                player.registryAccess()
                        );

                        return Optional.of(new CultivationState(
                                progress,
                                resolveContent(cultivationSkill.primaryPath())
                        ));
                    });
                })
        );
    }

    private static double resolveProgress(
            PathData pathData,
            Identifier pathId,
            boolean cultivationSuppressed,
            net.minecraft.core.RegistryAccess registryAccess
    ) {
        if (cultivationSuppressed && pathData instanceof FoundationPathData foundationData) {
            Path path = CoreRegistries.safeAccess(
                    CoreRegistries.PATH_REGISTRY,
                    pathId,
                    registryAccess
            );
            if (!(path instanceof FoundationPath foundationPath)) {
                return 0.0D;
            }

            int majorRealm = foundationData.getMajorRealm();
            int foundationRealm = foundationData.getCurrentFoundationRealm();
            double maximum = foundationPath.getMaxFoundationProgress(majorRealm, foundationRealm);
            return maximum <= 0.0D
                    ? 0.0D
                    : Math.clamp(foundationData.getCurrentFoundationProgress() / maximum, 0.0D, 1.0D);
        }

        double maximum = pathData.getMaxProgress(
                pathData.getMajorRealm(),
                pathData.getMinorRealm(),
                registryAccess
        );
        return maximum <= 0.0D
                ? 0.0D
                : Math.clamp(pathData.getProgress() / maximum, 0.0D, 1.0D);
    }

    private static ITextureData resolveContent(Identifier pathId) {
        String path = pathId.getPath();
        if (path.endsWith("/soul") || path.equals("soul")) {
            return SOUL_CONTENT;
        }
        if (path.endsWith("/body") || path.equals("body")) {
            return BODY_CONTENT;
        }
        return ESSENCE_CONTENT;
    }

    private record CultivationState(double progress, ITextureData content) {
    }
}
