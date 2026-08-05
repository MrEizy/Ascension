package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.common.gui.data.ClientAscensionData;

import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.Optional;

public class CultivationProgressBar extends RenderableElement {
    private static final Identifier FRAME_TEXTURE =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/main/overlays/progress_bar.png");

    private static final Identifier BARS_TEXTURE =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/main/overlays/progress_bars.png");

    private static final Identifier ESSENCE_PATH =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "foundation/essence");

    private static final Identifier SOUL_PATH =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "foundation/soul");

    private static final Identifier BODY_PATH =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "foundation/body");

    private static final int FRAME_WIDTH = 111;
    private static final int FRAME_HEIGHT = 53;

    private static final int CONTENT_X = 5;
    private static final int CONTENT_Y = 30;
    private static final int CONTENT_WIDTH = 101;
    private static final int CONTENT_HEIGHT = 9;

    private static final ITextureData BACKGROUND =
            new TextureDataSubsection(
                    FRAME_TEXTURE,
                    FRAME_WIDTH,
                    FRAME_HEIGHT,
                    0,
                    0,
                    FRAME_WIDTH,
                    FRAME_HEIGHT
            );

    private static final ITextureData SOUL_CONTENT =
            new TextureDataSubsection(
                    BARS_TEXTURE,
                    111,
                    52,
                    0,
                    0,
                    CONTENT_WIDTH,
                    CONTENT_HEIGHT
            );

    private static final ITextureData BODY_CONTENT =
            new TextureDataSubsection(
                    BARS_TEXTURE,
                    111,
                    52,
                    0,
                    11,
                    CONTENT_WIDTH,
                    CONTENT_HEIGHT
            );

    private static final ITextureData ESSENCE_CONTENT =
            new TextureDataSubsection(
                    BARS_TEXTURE,
                    111,
                    52,
                    0,
                    22,
                    CONTENT_WIDTH,
                    CONTENT_HEIGHT
            );

    private static final ITextureData OTHER_CONTENT =
            new TextureDataSubsection(
                    BARS_TEXTURE,
                    111,
                    52,
                    0,
                    33,
                    CONTENT_WIDTH,
                    CONTENT_HEIGHT
            );

    public CultivationProgressBar(UIFrame frame) {
        super(frame);

        setWidth(FRAME_WIDTH);
        setHeight(FRAME_HEIGHT);

        getPositioning().setXPositioningRule(PositioningRules.CENTER);
        getPositioning().setYPositioningRule(PositioningRules.END);
        getPositioning().setX(-FRAME_WIDTH / 2);
        getPositioning().setY(86);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Optional<CultivationState> optionalState = getState();
        if (optionalState.isEmpty()) {
            return;
        }

        CultivationState state = optionalState.get();

        BACKGROUND.render(graphics);

        int filledWidth = (int) Math.round(
                CONTENT_WIDTH * state.progress()
        );

        filledWidth = Math.clamp(filledWidth, 0, CONTENT_WIDTH);

        if (filledWidth > 0) {
            state.content().renderAt(
                    graphics,
                    CONTENT_X,
                    CONTENT_Y,
                    filledWidth,
                    CONTENT_HEIGHT
            );
        }
    }

    private Optional<CultivationState> getState() {
        return ClientAscensionData.getPlayer().flatMap(player -> {
            boolean castHeld = player.getData(ZenithAttachments.ACTION_MANAGER).isActive(AscensionSkillListener.skillCast);

            if (!castHeld) {
                return Optional.empty();
            }

            return ClientAscensionData.getSkillCastHandler().flatMap(handler -> {
                Identifier castingSkillId = handler.getCastingSkill();

                Identifier activeSkillId = castingSkillId != null ? castingSkillId : handler.getSkill(handler.getSelectedSlot());

                if (activeSkillId == null) {
                    return Optional.empty();
                }

                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, activeSkillId, player.registryAccess());

                if (!(skill instanceof SimpleCultivationSkill cultivationSkill)) {
                    return Optional.empty();
                }

                Identifier pathId = cultivationSkill.primaryPath();

                return ClientAscensionData.getEntityData().flatMap(entityData -> {
                    PathInstance PathInstance = AscensionOriginSourceHelper.getPathInstance(entityData.getSource(),pathId);

                    if (PathInstance == null) {
                        return Optional.empty();
                    }

                    double progress = resolveProgress(PathInstance, pathId, entityData.isCultivationSuppressed(), player.registryAccess());

                    return Optional.of(new CultivationState(progress, resolveContent(pathId)));
                });
            });
        });
    }

    private static double resolveProgress(PathInstance pathInstance, Identifier pathId, boolean cultivationSuppressed, net.minecraft.core.RegistryAccess registryAccess) {
        /*
        if (cultivationSuppressed && pathInstance instanceof FoundationPathInstance foundationData) {

            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, registryAccess);

            if (!(path instanceof FoundationPath foundationPath)) {
                return 0.0D;
            }

            int majorRealm = foundationData.getMajorRealm();
            int foundationRealm = foundationData.getCurrentFoundationRealm();

            double maximum = foundationPath.getMaxFoundationProgress(majorRealm, foundationRealm);

            if (maximum <= 0.0D) {
                return 0.0D;
            }

            return Math.clamp(foundationData.getCurrentFoundationProgress() / maximum, 0.0D, 1.0D);
        }
         */

        double maximum = pathInstance.getMaxProgress();

        if (maximum <= 0.0D) {
            return 0.0D;
        }

        return Math.clamp(pathInstance.getProgress() / maximum, 0.0D, 1.0D);
    }

    private static ITextureData resolveContent(Identifier pathId) {
        if (SOUL_PATH.equals(pathId)) {
            return SOUL_CONTENT;
        }

        if (BODY_PATH.equals(pathId)) {
            return BODY_CONTENT;
        }

        if (ESSENCE_PATH.equals(pathId)) {
            return ESSENCE_CONTENT;
        }

        return OTHER_CONTENT;
    }

    private record CultivationState(double progress, ITextureData content) {
    }
}