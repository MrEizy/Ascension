package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingEffects;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import net.zic.ascension.client.renderer.IrisPipelineCompat;
import net.zic.ascension.client.renderer.LightmapColor;
import net.zic.ascension.client.renderer.ModRenderTypes;
import net.zic.ascension.client.renderer.QiCoatingLooks;
import net.zic.ascension.client.visual.QiCoatingClientState;
import net.zic.ascension.client.visual.runtime.QiBarrierVisualController;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Draws players' Qi Coatings (QiCoatingDefinition, loaded from datapacks; the looks they name are in QiCoatingLooks): the shell
 * looks' boxes round the arms or every part, the skin looks painted into the player's own skin pixels, the echoing
 * arms and the coded effects (QiCoatingEffects). Geometry is built in each part's local model space, so it follows every
 * animation, in third and first person.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public class QiCoatingLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    // Arm cubes run from y = -2 (shoulder) to y = 10 (hand tip), 12px long.
    static final float HAND_TIP_Y = 10.0F;
    // How far up the arm a gathering coating with no shells (just a skin look) has its specks land, in pixels from the hand tip.
    private static final float GATHER_SKIN_REACH = 7.0F;
    static final float SHOULDER_Y = -2.0F;
    // Whole-body echoes are scattered further than hand echoes so they read at body scale.
    private static final float BODY_ECHO_SPREAD = 1.5F;
    static final float ARM_MIN_Z = -2.0F;
    static final float ARM_MAX_Z = 2.0F;
    // Skin looks (QiCoatingDefinition.skin) redraw each skin layer this far (in pixels) out from its own surface,
    // just enough to stay in front of the skin they cover without visibly lifting off it. Corners move out on every axis, like
    // vanilla's inflated skin layer, so the faces stay joined.
    private static final float SKIN_COAT_LIFT = 0.03F;
    // Whole-skin layers (the Golden Body) pour on in one wave: the share {start, end} of the growth each part spreads over. The
    // legs fill from the feet first, the torso from the waist once they reach the hips, and the head from the neck last; the
    // arms fill from the hands meanwhile, reaching the shoulders about as the torso does.
    private static final float[] LEGS_POUR = {0.0F, 0.4F};
    private static final float[] TORSO_POUR = {0.4F, 0.8F};
    private static final float[] HEAD_POUR = {0.8F, 1.0F};
    private static final float[] ARMS_POUR = {0.1F, 0.8F};
    private static final Map<SkinLimbKey, ModelPart> SKIN_LIMBS = new HashMap<>();
    // Coatings react to what they list in reacts_to: the player swinging (that arm) or being hurt (both arms). Looks with a hit
    // animation play it over the IMPACT_MILLIS after (must match IMPACT_SECONDS in include/qi_impact.glsl): the ice cracks and a
    // piece breaks off and refreezes, the metal sparks or rings. Kept per player id (Reactions), with what the player was doing
    // last tick to spot a new swing or hurt.
    private static final long IMPACT_MILLIS = 2000L;
    // Impact data for a limb that hasn't been hit lately: the oldest age, seed 0. See impactData.
    public static final int NO_IMPACT = 0xFF00;
    private static final Map<Integer, Reactions> REACTIONS = new HashMap<>();

    // The Qi Barrier's film: a box round every part, BARRIER_INFLATE pixels out, on the skin as the Pure Qi coating's shell is on
    // the hand (its inflate), so the skin's outer layer (sleeves, jacket, hat) sits over it. It never shares the body with a
    // hand coating (each skill turns the other off). (Not a QiCoatingDefinition.Body: building one here, as this class loads,
    // loads Body before QiCoatingDefinition, whose codecs then read Body's before it's made.)
    private static final float BARRIER_INFLATE = 0.1F;

    // Coatings that echo: faint violet copies of the arm, re-scattered every ECHO_STEP_MILLIS, up to ECHO_SPREAD pixels away.
    private static final int ECHO_COUNT = 2;
    private static final long ECHO_STEP_MILLIS = 90L;
    private static final float ECHO_SPREAD = 0.9F;
    private static final int ECHO_TINT = 0x59B98CFF;
    // Coatings that echo and react to something (reacts_to) only echo for ECHO_TRIGGER_MILLIS after it happens.
    private static final long ECHO_TRIGGER_MILLIS = 700L;

    // What each coating effect (a coating's effects, QiCoatingEffects) does here, by id. Unknown ids do nothing.
    private static final Map<Identifier, QiCoatingEffect> EFFECTS = new LinkedHashMap<>();

    static {
        registerEffect(QiCoatingEffects.LIGHTNING_STEP, LightningStepVisuals.INSTANCE);
        registerEffect(QiCoatingEffects.WIND_RINGS, new WindRingsEffect());
    }

    public QiCoatingLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    /**
     * Gives a coating effect id its client side. Other mods can add theirs during client setup.
     */
    public static void registerEffect(Identifier id, QiCoatingEffect effect) {
        EFFECTS.put(id, effect);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Fires on every resource reload, so rebake the skin-fitted limbs (and ghost arms) from the new model set.
        SKIN_LIMBS.clear();
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new QiCoatingLayer(renderer));
            }
        }
    }

    /**
     * First person: vanilla only draws the bare arm when the hand is empty, and this fires right before it.
     */
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        QiCoating found = QiCoatingClientState.get(player.getUUID(), QiCoatingChannel.BODY);
        QiCoating coating = found == null || found.swordQi() ? null : found;
        QiBarrierVisualController.Film film = QiBarrierVisualController.film(player.getUUID());
        QiCoatingLooks.ShellLook barrierLook = film == null ? null : QiCoatingLooks.shell(QiBarrierVisualController.ID);
        if (coating == null && barrierLook == null) {
            return;
        }
        AvatarRenderer<AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        ModelPart arm = renderer.getModel().getArm(event.getArm());

        // Mirror AvatarRenderer.renderHand, which resets the arm to its rest pose before drawing it.
        arm.resetPose();
        arm.zRot = event.getArm() == HumanoidArm.LEFT ? -0.1F : 0.1F;

        boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        if (coating != null && bodyAlpha(coating) < 1.0F) {
            // Replace vanilla's opaque hand with a translucent one (the effect draws it, QiCoatingEffect.submitArm).
            event.setCanceled(true);
        }
        PlayerModelPart sleevePart = event.getArm() == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE;
        boolean sleeve = player.isModelPartShown(sleevePart);
        Identifier skin = player.getSkin().body().texturePath();
        if (!event.isCanceled() && IrisPipelineCompat.isShaderPackInUse()) {
            // Under a shader pack the coating is drawn after the pack (AfterShaderPackDraws), where the depth doesn't hold the
            // pack's hand: the arm goes into it first, so the hand still hides the coating's far side and inner faces.
            ModelPart depthArm = ghostArm(slim, event.getArm(), sleeve);
            int light = event.getPackedLight();
            AfterShaderPackDraws.submitCustomGeometry(event.getSubmitNodeCollector(), poseStack, ModRenderTypes.qiCoatingArmDepth(skin),
                    (pose, buffer) -> {
                        PoseStack armPose = new PoseStack();
                        armPose.last().set(pose);
                        depthArm.render(armPose, buffer, light, OverlayTexture.NO_OVERLAY);
                    });
        }
        if (coating != null) {
            submitCoatedArm(poseStack, event.getSubmitNodeCollector(), coating, skin, event.getPackedLight(),
                    OverlayTexture.NO_OVERLAY, event.getArm(), slim, sleeve, player.getId());
        }
        if (barrierLook != null) {
            // The Qi Barrier's film round the arm, as in third person (submitBarrierFilm).
            float minX = armMinX(event.getArm(), slim);
            float maxX = minX + armWidth(slim);
            float styleOffset = barrierLook.style() * 2.0F;
            int color = film.color();
            AfterShaderPackDraws.submitCustomGeometry(event.getSubmitNodeCollector(), poseStack, renderType(barrierLook), (pose, buffer) ->
                    ShellGeometry.emitBox(pose, buffer, minX - BARRIER_INFLATE, maxX + BARRIER_INFLATE, SHOULDER_Y - BARRIER_INFLATE,
                            HAND_TIP_Y + BARRIER_INFLATE, ARM_MIN_Z - BARRIER_INFLATE, ARM_MAX_Z + BARRIER_INFLATE, styleOffset, color,
                            true, ShellGeometry.CAP_V_SPAN));
        }
        poseStack.popPose();
    }

    /**
     * Per-tick work for each player's coating: its grow clock, hit reactions and
     * Lightning Step's footprints.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            return;
        }
        for (AbstractClientPlayer player : level.players()) {
            // A body coating and a weapon coating can both be on, each on its own channel.
            QiCoating body = null;
            QiCoating external = null;
            for (QiCoatingChannel channel : QiCoatingChannel.values()) {
                QiCoating coating = QiCoatingClientState.get(player.getUUID(), channel);
                QiCoatingClientState.trackActivation(player.getUUID(), channel, QiCoatingClientState.live(player.getUUID(), channel));
                if (coating != null && !player.isInvisible()) {
                    if (channel == QiCoatingChannel.BODY) {
                        body = coating;
                    } else {
                        external = coating;
                    }
                }
            }
            if ((body != null && body.reacts()) || (external != null && external.reacts())) {
                watchReactions(player);
            } else {
                REACTIONS.remove(player.getId());
            }
            for (Map.Entry<Identifier, QiCoatingEffect> effect : EFFECTS.entrySet()) {
                boolean active = (body != null && body.hasEffect(effect.getKey())) || (external != null && external.hasEffect(effect.getKey()));
                effect.getValue().tickPlayer(level, player, active);
            }
        }
        // Players who left the level.
        REACTIONS.keySet().removeIf(id -> level.getEntity(id) == null);
        for (QiCoatingEffect effect : EFFECTS.values()) {
            effect.tickLevel(level);
        }
    }

    /**
     * Coatings that react: notes a swing on the arm the player just swung, and a hurt on both arms when they were just hurt.
     */
    private static void watchReactions(AbstractClientPlayer player) {
        Reactions reactions = REACTIONS.computeIfAbsent(player.getId(), _ -> new Reactions(player.swinging, player.hurtTime));
        long now = Util.getMillis();
        RandomSource random = player.getRandom();
        if (player.swinging && !reactions.swinging) {
            HumanoidArm arm = player.swingingArm == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            reactions.record(QiCoatingDefinition.Reaction.ATTACK, arm == HumanoidArm.RIGHT ? 0 : 1, new Impact(now, random.nextInt(128)));
        }
        if (player.hurtTime > reactions.hurtTime) {
            reactions.record(QiCoatingDefinition.Reaction.HURT, 0, new Impact(now, random.nextInt(128)));
            reactions.record(QiCoatingDefinition.Reaction.HURT, 1, new Impact(now, random.nextInt(128)));
        }
        reactions.swinging = player.swinging;
        reactions.hurtTime = player.hurtTime;
    }

    /**
     * Whether the entity's coating is echoing right now: always for an echoing coating that reacts to nothing, and for a moment
     * after one of its reactions (reacts_to) for one that does. Never for a coating that doesn't echo.
     */
    public static boolean echoing(QiCoating coating, int entityId) {
        if (!coating.echoes()) {
            return false;
        }
        if (!coating.reacts()) {
            return true;
        }
        Impact latest = latestReaction(coating, entityId, Reactions.EITHER_ARM);
        return latest != null && Util.getMillis() - latest.startMillis() < ECHO_TRIGGER_MILLIS;
    }

    /**
     * A limb's last hit for qi_coating_skin.fsh, out of the reactions the coating lists. See impactData(Impact).
     *
     * @param arm 0 for the right arm, 1 for the left
     */
    private static int impactData(QiCoating coating, int entityId, int arm) {
        return impactData(latestReaction(coating, entityId, arm));
    }

    /**
     * impactData for whichever arm was hit last, for Sword Qi: the weapon breaks with the arm that swings it.
     */
    public static int latestImpactData(QiCoating coating, int entityId) {
        return impactData(latestReaction(coating, entityId, Reactions.EITHER_ARM));
    }

    /**
     * How long ago the hit was, as a share of IMPACT_MILLIS from 0 to 255 (255 when not recently), in the second byte, and its
     * random seed (0 to 127) in the low byte.
     */
    private static int impactData(Impact impact) {
        if (impact == null) {
            return NO_IMPACT;
        }
        long age = Util.getMillis() - impact.startMillis();
        if (age >= IMPACT_MILLIS) {
            return NO_IMPACT;
        }
        return Math.round((float) age / IMPACT_MILLIS * 254.0F) << 8 | impact.seed();
    }

    private static Impact latestReaction(QiCoating coating, int entityId, int arm) {
        Reactions reactions = REACTIONS.get(entityId);
        return reactions == null ? null : reactions.latest(coating, arm);
    }


    /**
     * Called from LivingEntityRendererQiCoatingMixin: how opaque the player's own model is drawn (1 as normal), lowered by
     * effects such as Lightning Step's (QiCoatingEffect.bodyAlpha).
     */
    public static float bodyAlpha(AvatarRenderState state) {
        if (state.isInvisible) {
            return 1.0F;
        }
        QiCoating coating = coatingFor(state);
        return coating == null ? 1.0F : bodyAlpha(coating);
    }

    private static float bodyAlpha(QiCoating coating) {
        float alpha = 1.0F;
        for (Identifier id : coating.definition().effects()) {
            QiCoatingEffect effect = EFFECTS.get(id);
            if (effect != null) {
                alpha = Math.min(alpha, effect.bodyAlpha());
            }
        }
        return alpha;
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        boolean slim = state.skin.model() == PlayerModelType.SLIM;
        submitBarrierFilm(poseStack, submitNodeCollector, state, slim);
        QiCoating coating = coatingFor(state);
        if (coating == null || coating.swordQi()) {
            return;
        }
        if (coating.wholeBody()) {
            submitBodyLayer(poseStack, submitNodeCollector, lightCoords, state, coating, slim);
            return;
        }
        submitArm(poseStack, submitNodeCollector, lightCoords, state, coating, HumanoidArm.RIGHT, slim);
        submitArm(poseStack, submitNodeCollector, lightCoords, state, coating, HumanoidArm.LEFT, slim);
    }

    /**
     * The Qi Barrier (QiBarrierVisualController) in third person: a film of plain Pure Qi over the whole body (never with a
     * hand coating: the skills turn each other off), its opacity and hit flash in the vertex colour (qi_coating.fsh style 59).
     */
    private void submitBarrierFilm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state, boolean slim) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null || state.isSpectator ? null : minecraft.level.getEntity(state.id);
        QiBarrierVisualController.Film film = entity == null ? null : QiBarrierVisualController.film(entity.getUUID());
        QiCoatingLooks.ShellLook look = QiCoatingLooks.shell(QiBarrierVisualController.ID);
        if (film == null || look == null) {
            return;
        }
        submitFilmShells(poseStack, submitNodeCollector, this.getParentModel(), look, BARRIER_INFLATE, slim, film.color());
    }

    /**
     * Qi Layer in third person: the skin look on the limbs, the shells over the body (Shells.body), then
     * Lightning Step's ground effects and (for echo layers) faint copies of the whole model scattered around it.
     */
    private void submitBodyLayer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state,
                                 QiCoating coating, boolean slim) {
        if (state.isSpectator) {
            return;
        }
        PlayerModel model = this.getParentModel();
        if (coating.definition().skin().isPresent()) {
            submitSkinLimbs(poseStack, submitNodeCollector, model, state, coating, lightCoords, slim);
        }
        float growth = freezeProgress(coating, state.id);
        boolean echoing = echoing(coating, state.id);
        submitBodyShells(poseStack, submitNodeCollector, model, coating, slim, lightCoords, growth, !echoing);
        for (Identifier id : coating.definition().effects()) {
            QiCoatingEffect effect = EFFECTS.get(id);
            if (effect != null) {
                effect.submitBody(poseStack, submitNodeCollector, state);
            }
        }
        if (!echoing) {
            return;
        }
        RenderType echoType = RenderTypes.entityTranslucent(state.skin.body().texturePath());
        int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        long step = Util.getMillis() / ECHO_STEP_MILLIS;
        for (int echo = 0; echo < ECHO_COUNT; echo++) {
            RandomSource random = RandomSource.create(step * 31L + echo * 7L + state.id);
            float x = (random.nextFloat() - 0.5F) * 2.0F * BODY_ECHO_SPREAD;
            float y = (random.nextFloat() - 0.5F) * BODY_ECHO_SPREAD;
            float z = (random.nextFloat() - 0.5F) * 2.0F * BODY_ECHO_SPREAD;
            poseStack.pushPose();
            poseStack.translate(x / 16.0F, y / 16.0F, z / 16.0F);
            // The model is posed from the render state again when this deferred draw runs, so the echo matches the player.
            submitNodeCollector.submitModel(model, state, poseStack, echoType, lightCoords, overlayCoords, ECHO_TINT, null, 0, null);
            submitBodyShells(poseStack, submitNodeCollector, model, coating, slim, lightCoords, growth, false);
            poseStack.popPose();
        }
    }

    /**
     * A body coating's shells in third person, as its Shells.body says: a film over every part (or just the limbs), or a box
     * round each limb with the look's per-limb pattern. Nothing if it has no body shells or its look is unknown.
     *
     * @param quiet whether to draw the shells' quiet look (if they have one), for a triggered echo coating that isn't echoing
     */
    private static void submitBodyShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                         QiCoating coating, boolean slim, int lightCoords, float growth, boolean quiet) {
        QiCoatingDefinition.Shells shells = coating.definition().shells().orElse(null);
        if (shells == null || shells.body().isEmpty()) {
            return;
        }
        QiCoatingDefinition.Body body = shells.body().get();
        if (shells.texture().isPresent()) {
            QiCoatingTextureShells.submitBody(poseStack, submitNodeCollector, model, shells.texture().get(), body, slim, lightCoords,
                    growth);
            return;
        }
        QiCoatingLooks.ShellLook look = QiCoatingLooks.shell(shells.look(quiet).orElseThrow());
        if (look == null) {
            return;
        }
        if (body.mode() == QiCoatingDefinition.BodyMode.LIMBS) {
            submitLimbPatternShells(poseStack, submitNodeCollector, model, look, body, slim);
        } else {
            submitFilmShells(poseStack, submitNodeCollector, model, look, body.inflate(), slim, 0xFFFFFFFF);
        }
    }

    /**
     * A film over every body part, `inflate` pixels out, with every box in `color` (looks read it as their tint and opacity, or
     * as data, as the Qi Barrier does).
     */
    private static void submitFilmShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                         QiCoatingLooks.ShellLook look, float inflate, boolean slim, int color) {
        RenderType renderType = renderType(look);
        float styleOffset = look.style() * 2.0F;
        // Cube bounds from HumanoidModel/PlayerModel.createMesh, in each part's local space.
        float armWidth = armWidth(slim);
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        submitPartShell(poseStack, submitNodeCollector, model.head, renderType, styleOffset,
                -4.0F, 4.0F, -8.0F, 0.0F, -4.0F, 4.0F, true, inflate, false, color);
        submitPartShell(poseStack, submitNodeCollector, model.body, renderType, styleOffset,
                -4.0F, 4.0F, 0.0F, 12.0F, -2.0F, 2.0F, true, inflate, false, color);
        submitPartShell(poseStack, submitNodeCollector, model.rightArm, renderType, styleOffset,
                1.0F - armWidth, 1.0F, SHOULDER_Y, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, true, inflate, false, color);
        submitPartShell(poseStack, submitNodeCollector, model.leftArm, renderType, styleOffset,
                -1.0F, armWidth - 1.0F, SHOULDER_Y, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, true, inflate, false, color);
        submitPartShell(poseStack, submitNodeCollector, model.rightLeg, renderType, styleOffset,
                -2.0F, 2.0F, 0.0F, 12.0F, -2.0F, 2.0F, true, inflate, false, color);
        submitPartShell(poseStack, submitNodeCollector, model.leftLeg, renderType, styleOffset,
                -2.0F, 2.0F, 0.0F, 12.0F, -2.0F, 2.0F, true, inflate, false, color);
        poseStack.popPose();
    }

    /**
     * A box round each arm and leg (or just each leg) with the look's per-limb pattern, such as the lightning limbs'
     * bolts. Each limb gets its own style (limbStyleOffset), so no two limbs repeat.
     */
    private static void submitLimbPatternShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                                QiCoatingLooks.ShellLook look, QiCoatingDefinition.Body body, boolean slim) {
        RenderType renderType = renderType(look);
        float inflate = body.inflate();
        float armWidth = armWidth(slim);
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        if (!body.legsOnly()) {
            submitPartShell(poseStack, submitNodeCollector, model.rightArm, renderType, limbStyleOffset(look, 0),
                    1.0F - armWidth, 1.0F, SHOULDER_Y, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, false, inflate, true);
            submitPartShell(poseStack, submitNodeCollector, model.leftArm, renderType, limbStyleOffset(look, 1),
                    -1.0F, armWidth - 1.0F, SHOULDER_Y, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, false, inflate, true);
        }
        submitPartShell(poseStack, submitNodeCollector, model.rightLeg, renderType, limbStyleOffset(look, 2),
                -2.0F, 2.0F, 0.0F, 12.0F, -2.0F, 2.0F, false, inflate, true);
        submitPartShell(poseStack, submitNodeCollector, model.leftLeg, renderType, limbStyleOffset(look, 3),
                -2.0F, 2.0F, 0.0F, 12.0F, -2.0F, 2.0F, false, inflate, true);
        poseStack.popPose();
    }

    /**
     * A skin look on the body in third person: on the arms and legs, or the whole skin (head and torso too). Each part is posed
     * like the player's now; the outer layer is only redrawn where the player shows it.
     */
    private static void submitSkinLimbs(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                        AvatarRenderState state, QiCoating coating, int lightCoords, boolean slim) {
        Identifier skin = state.skin.body().texturePath();
        float progress = freezeProgress(coating, state.id);
        // Whole-skin layers pour on in one wave (and melt off in reverse): each part runs its own share of the growth.
        boolean staggered = coating.coversHeadAndTorso();
        float armProgress = staggered ? partProgress(progress, ARMS_POUR) : progress;
        float legProgress = staggered ? partProgress(progress, LEGS_POUR) : progress;
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        submitPosedSkinLimb(poseStack, submitNodeCollector, model.rightArm, skin, coating, armProgress, impactData(coating, state.id, 0), lightCoords,
                skinLimb(slim, "right_arm", state.showRightSleeve), armCenterX(HumanoidArm.RIGHT, slim), HAND_TIP_Y);
        submitPosedSkinLimb(poseStack, submitNodeCollector, model.leftArm, skin, coating, armProgress, impactData(coating, state.id, 1), lightCoords,
                skinLimb(slim, "left_arm", state.showLeftSleeve), armCenterX(HumanoidArm.LEFT, slim), HAND_TIP_Y);
        submitPosedSkinLimb(poseStack, submitNodeCollector, model.rightLeg, skin, coating, legProgress, NO_IMPACT, lightCoords,
                skinLimb(slim, "right_leg", state.showRightPants), 0.0F, 12.0F);
        submitPosedSkinLimb(poseStack, submitNodeCollector, model.leftLeg, skin, coating, legProgress, NO_IMPACT, lightCoords,
                skinLimb(slim, "left_leg", state.showLeftPants), 0.0F, 12.0F);
        if (staggered) {
            // The torso spreads from the waist (y = 12) up and the head from the neck (y = 0) up. Both are measured in the
            // limbs' 12px lengths, so the texel rows stay the size of the limbs'; the 8px head only reaches two thirds of the way.
            submitPosedSkinLimb(poseStack, submitNodeCollector, model.body, skin, coating, partProgress(progress, TORSO_POUR),
                    NO_IMPACT, lightCoords, skinLimb(slim, "body", state.showJacket), 0.0F, 12.0F);
            submitPosedSkinLimb(poseStack, submitNodeCollector, model.head, skin, coating, partProgress(progress, HEAD_POUR),
                    NO_IMPACT, lightCoords, skinLimb(slim, "head", state.showHat), 0.0F, 0.0F);
        }
        poseStack.popPose();
    }

    /**
     * How far one part has grown, 0 to 1, when the whole coating is `progress` along and the part grows over the share
     * {start, end} of it.
     */
    private static float partProgress(float progress, float[] share) {
        return Mth.clamp((progress - share[0]) / (share[1] - share[0]), 0.0F, 1.0F);
    }

    private static void submitPosedSkinLimb(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPart part, Identifier skin,
                                            QiCoating coating, float progress, int impact, int lightCoords, ModelPart limb,
                                            float centerX, float tipY) {
        if (!part.visible) {
            return;
        }
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        submitSkinLimb(poseStack, submitNodeCollector, skin, coating, progress, impact, lightCoords, limb, centerX, tipY);
        poseStack.popPose();
    }

    // From PlayerModel.createMesh.
    static float armWidth(boolean slim) {
        return slim ? 3.0F : 4.0F;
    }

    /**
     * Where an arm's cube starts across its width, in its local pixels: the right arm's reaches 1 past its pivot, the left
     * arm's starts 1 before it.
     */
    static float armMinX(HumanoidArm side, boolean slim) {
        return side == HumanoidArm.RIGHT ? 1.0F - armWidth(slim) : -1.0F;
    }

    static float armCenterX(HumanoidArm side, boolean slim) {
        return armMinX(side, slim) + armWidth(slim) * 0.5F;
    }

    /**
     * Redraws one limb from the skin, both layers, a hair off each surface, for the coating's skin look (qi_coating_skin.fsh)
     * to paint into. Expects the pose stack to already be in the limb's local space. Nothing if the coating has no skin look
     * or it is unknown.
     *
     * @param impact  the limb's last hit, from impactData (NO_IMPACT for none)
     * @param limb    a private copy of the limb from skinLimb
     * @param centerX the middle of the limb's cube across its width, in its local pixels
     * @param tipY    the limb's hand or foot end in its local pixels, where the freeze starts
     */
    private static void submitSkinLimb(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier skin,
                                       QiCoating coating, float progress, int impact, int lightCoords, ModelPart limb,
                                       float centerX, float tipY) {
        submitSkinLimb(poseStack, submitNodeCollector, skin, coating, progress, impact, lightCoords, limb, centerX, tipY, null);
    }

    /**
     * @param patches if set, only the skin pixels round its landings are coated; the rest show the plain limb underneath
     */
    private static void submitSkinLimb(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier skin,
                                       QiCoating coating, float progress, int impact, int lightCoords, ModelPart limb,
                                       float centerX, float tipY, ShellGeometry.Patches patches) {
        RenderPipeline pipeline = coating.definition().skin().map(look -> QiCoatingLooks.skin(look.look())).orElse(null);
        if (pipeline == null) {
            return;
        }
        boolean wholeBody = coating.wholeBody();
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoatingSkin(pipeline, skin), (pose, buffer) -> {
            PoseStack limbPose = new PoseStack();
            limbPose.last().set(pose);
            limb.render(limbPose, new SkinCoatingConsumer(buffer, pose.pose(), wholeBody, progress, impact, centerX, tipY, patches),
                    lightCoords, OverlayTexture.NO_OVERLAY);
        });
    }

    /**
     * A private copy of one of the player's limbs (or the torso or head), with or without its outer layer, baked at the origin.
     * Like ghostArm, these are never re-posed, so they are safe to draw deferred.
     */
    private static ModelPart skinLimb(boolean slim, String name, boolean outerLayer) {
        return SKIN_LIMBS.computeIfAbsent(new SkinLimbKey(slim, name, outerLayer), key -> {
            ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(key.slim() ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER);
            ModelPart limb = root.getChild(key.name());
            limb.setInitialPose(PartPose.ZERO);
            limb.resetPose();
            String outer = switch (key.name()) {
                case "right_arm" -> "right_sleeve";
                case "left_arm" -> "left_sleeve";
                case "right_leg" -> "right_pants";
                case "left_leg" -> "left_pants";
                case "body" -> "jacket";
                default -> "hat";
            };
            limb.getChild(outer).visible = key.outerLayer();
            return limb;
        });
    }

    /**
     * How far a coating that grows (QiCoating.grows) has grown, 0 to 1: rising after it comes on and falling again as it shrinks
     * away after it goes off (QiCoatingClientState.growth). Always 1 for coatings that don't grow.
     */
    private static float freezeProgress(QiCoating coating, int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        if (entity == null) {
            return 1.0F;
        }
        return QiCoatingClientState.growth(entity.getUUID(), coating);
    }

    // limb: 0 right arm, 1 left arm, 2 right leg, 3 left leg.
    private static float limbStyleOffset(QiCoatingLooks.ShellLook look, int limb) {
        return look.limbStyle(limb) * 2.0F;
    }

    /**
     * @param squareCap whether the caps are mapped so a square texel grid on the sides stays square on them, instead of
     *                  ShellGeometry.CAP_V_SPAN
     */
    private static void submitPartShell(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPart part, RenderType renderType,
                                        float styleOffset, float minX, float maxX, float minY, float maxY, float minZ, float maxZ,
                                        boolean topCap, float inflate, boolean squareCap) {
        submitPartShell(poseStack, submitNodeCollector, part, renderType, styleOffset, minX, maxX, minY, maxY, minZ, maxZ,
                topCap, inflate, squareCap, 0xFFFFFFFF);
    }

    private static void submitPartShell(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPart part, RenderType renderType,
                                        float styleOffset, float minX, float maxX, float minY, float maxY, float minZ, float maxZ,
                                        boolean topCap, float inflate, boolean squareCap, int color) {
        if (!part.visible) {
            return;
        }
        float capVSpan = squareCap ? (maxZ - minZ + 2.0F * inflate) / (maxY - minY + 2.0F * inflate) : ShellGeometry.CAP_V_SPAN;
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, renderType, (pose, buffer) ->
                ShellGeometry.emitBox(pose, buffer, minX - inflate, maxX + inflate, minY - inflate, maxY + inflate, minZ - inflate, maxZ + inflate,
                        styleOffset, color, topCap, capVSpan));
        poseStack.popPose();
    }

    private static QiCoating coatingFor(AvatarRenderState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        return entity == null ? null : QiCoatingClientState.get(entity.getUUID(), QiCoatingChannel.BODY);
    }

    private void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state,
                           QiCoating coating, HumanoidArm side, boolean slim) {
        if (state.isSpectator) {
            return;
        }
        PlayerModel model = this.getParentModel();
        ModelPart arm = model.getArm(side);
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        arm.translateAndRotate(poseStack);
        boolean sleeve = side == HumanoidArm.RIGHT ? state.showRightSleeve : state.showLeftSleeve;
        submitCoatedArm(poseStack, submitNodeCollector, coating, state.skin.body().texturePath(), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), side, slim, sleeve, state.id);
        poseStack.popPose();
    }

    /**
     * One arm of a coating: a hands coating's in third person, or any coating's in first person. In order: the skin look, the
     * shells, their echoes, and what the coating's effects draw (QiCoatingEffect.submitArm).
     * Expects the pose stack to already be in the arm's local space.
     */
    private static void submitCoatedArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating,
                                        Identifier skinTexture, int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve, int seed) {
        float growth = freezeProgress(coating, seed);
        // A gathering coating (QiCoatingDefinition.Gather) forms from specks drawn in round the arm while it grows; a FADE one
        // fades its skin look and wind rings in with its shells.
        QiCoatingDefinition.Gather gather = coating.definition().gather().orElse(null);
        boolean fade = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.FADE;
        ShellGeometry.Patches patches = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.PATCHES
                ? new ShellGeometry.Patches(QiCoatingGather.landings(gather, side, slim, gatherReach(coating)), growth)
                : null;
        if (gather != null) {
            QiCoatingGather.submitSpecks(poseStack, submitNodeCollector, gather, side, slim, gatherReach(coating), growth);
        }
        if (coating.definition().skin().isPresent()) {
            // In patches the skin look is drawn fully formed, just on the pixels revealed so far.
            float skinProgress = fade ? QiCoatingGather.fadeAmount(growth) : patches != null ? 1.0F : growth;
            submitSkinLimb(poseStack, submitNodeCollector, skinTexture, coating, skinProgress,
                    impactData(coating, seed, side == HumanoidArm.RIGHT ? 0 : 1), lightCoords,
                    skinLimb(slim, side == HumanoidArm.RIGHT ? "right_arm" : "left_arm", sleeve), armCenterX(side, slim), HAND_TIP_Y,
                    patches);
        }
        boolean echoing = echoing(coating, seed);
        submitShells(poseStack, submitNodeCollector, coating, side, slim, lightCoords, growth, !echoing, seed);
        if (echoing) {
            submitEchoes(poseStack, submitNodeCollector, coating, skinTexture, lightCoords, overlayCoords, side, slim, sleeve, growth, seed);
        }
        if (!coating.definition().effects().isEmpty()) {
            QiCoatingEffect.Arm arm = new QiCoatingEffect.Arm(side, slim, sleeve, skinTexture, lightCoords, overlayCoords,
                    fade || patches != null ? QiCoatingGather.fadeAmount(growth) : 1.0F);
            for (Identifier id : coating.definition().effects()) {
                QiCoatingEffect effect = EFFECTS.get(id);
                if (effect != null) {
                    effect.submitArm(poseStack, submitNodeCollector, arm);
                }
            }
        }
    }

    /**
     * Faint violet copies of the arm and coating that jump to a new offset several times a second.
     */
    private static void submitEchoes(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating,
                                     Identifier skinTexture, int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve,
                                     float growth, int entityId) {
        long step = Util.getMillis() / ECHO_STEP_MILLIS;
        for (int echo = 0; echo < ECHO_COUNT; echo++) {
            RandomSource random = RandomSource.create(step * 31L + echo * 7L + side.ordinal());
            float x = (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD;
            float y = (random.nextFloat() - 0.5F) * ECHO_SPREAD;
            float z = (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD;
            poseStack.pushPose();
            poseStack.translate(x / 16.0F, y / 16.0F, z / 16.0F);
            submitGhostArm(poseStack, submitNodeCollector, skinTexture, lightCoords, overlayCoords, side, slim, sleeve, ECHO_TINT);
            submitShells(poseStack, submitNodeCollector, coating, side, slim, lightCoords, growth, false, entityId);
            poseStack.popPose();
        }
    }

    /**
     * A translucent copy of the arm in `tint` (its alpha too). Expects the pose stack to already be in the arm's local space.
     */
    static void submitGhostArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier skinTexture,
                                       int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve, int tint) {
        ModelPart arm = ghostArm(slim, side, sleeve);
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoatingEntityTranslucent(skinTexture), (pose, buffer) -> {
            PoseStack armPose = new PoseStack();
            armPose.last().set(pose);
            arm.render(armPose, buffer, lightCoords, overlayCoords, tint);
        });
    }

    /**
     * A private arm (with or without sleeve) baked at the origin, the same one skin coatings use (skinLimb). Model parts are
     * drawn deferred using their live pose, so the shared PlayerModel arm can't be used here; these copies are never re-posed,
     * making them safe to share.
     */
    private static ModelPart ghostArm(boolean slim, HumanoidArm side, boolean sleeve) {
        return skinLimb(slim, side == HumanoidArm.RIGHT ? "right_arm" : "left_arm", sleeve);
    }

    /**
     * A coating's shell layers round one arm (Shells.layers). Nothing if it has no shells or its look is unknown. Expects the
     * pose stack to already be in the arm's local space.
     *
     * @param growth how far the coating has grown (freezeProgress), which texture shells are cut to
     * @param quiet  whether to draw the shells' quiet look (if they have one), for a triggered echo coating that isn't echoing
     * @param entityId the player wearing it, whose biome a foliage look takes its colour from
     */
    private static void submitShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating, HumanoidArm side,
                                     boolean slim, int lightCoords, float growth, boolean quiet, int entityId) {
        QiCoatingDefinition.Shells shells = coating.definition().shells().orElse(null);
        if (shells == null || shells.layers().isEmpty()) {
            return;
        }
        QiCoatingDefinition.Gather gather = coating.definition().gather().orElse(null);
        if (shells.texture().isPresent()) {
            QiCoatingTextureShells.submitArm(poseStack, submitNodeCollector, shells.texture().get(), shells.layers(), side, slim,
                    lightCoords, growth, gather);
            return;
        }
        QiCoatingLooks.ShellLook look = QiCoatingLooks.shell(shells.look(quiet).orElseThrow());
        if (look == null) {
            return;
        }
        // Shell looks aren't cut as they grow; a gather fades their layers in, or splits them into pixels drawn round its landings.
        boolean fade = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.FADE;
        ShellGeometry.Patches patches = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.PATCHES
                ? new ShellGeometry.Patches(QiCoatingGather.landings(gather, side, slim, QiCoatingTextureShells.maxReach(shells.layers())), growth)
                : null;
        float minX = armMinX(side, slim);
        float maxX = minX + armWidth(slim);
        // Per-limb patterns start at their own point or style on each arm, so the two arms don't repeat each other.
        float shiftedStyle = look.perLimb()
                ? limbStyleOffset(look, side == HumanoidArm.RIGHT ? 0 : 1)
                : look.style() * 2.0F;
        List<QiCoatingDefinition.Layer> layers = shells.layers();
        int foliage = look.foliage() ? foliageTint(coating, entityId, lightCoords) : 0;
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, renderType(look), (pose, buffer) -> {
            for (QiCoatingDefinition.Layer layer : layers) {
                int color = look.foliage() ? foliage : layer.color();
                if (fade) {
                    color = QiCoatingGather.fade(color, growth);
                } else if (look.grows()) {
                    // A look that grows itself in reads how far along it is from alpha (qi_coating.fsh).
                    color = ARGB.color(Math.round(Mth.clamp(growth, 0.0F, 1.0F) * 255.0F), color);
                }
                ShellGeometry.emitShell(pose, buffer, minX, maxX, shiftedStyle, layer, color, patches);
            }
        });
    }

    /**
     * A foliage look's colour (ShellLook.foliage, SwordLook.foliage): the foliage colour of the biome the player stands in, as
     * vines and leaves take, held or eased as the coating says (QiCoatingClientState.foliage), under the world's light there,
     * opaque.
     */
    public static int foliageTint(QiCoating coating, int entityId, int lightCoords) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        int foliage = entity == null ? FoliageColor.FOLIAGE_DEFAULT : QiCoatingClientState.foliage(entity.getUUID(), coating,
                BiomeColors.getAverageFoliageColor(minecraft.level, entity.blockPosition()));
        return ARGB.opaque(ARGB.multiply(foliage, LightmapColor.of(lightCoords)));
    }

    private static RenderType renderType(QiCoatingLooks.ShellLook look) {
        return switch (look.render()) {
            case NORMAL -> ModRenderTypes.qiCoating();
            case DEPTH -> ModRenderTypes.qiCoatingDepth();
        };
    }

    /**
     * How far up the arm a gathering coating's specks land: its shells' reach, or the forearm for a skin look.
     */
    private static float gatherReach(QiCoating coating) {
        return coating.definition().shells()
                .filter(shells -> !shells.layers().isEmpty())
                .map(shells -> QiCoatingTextureShells.maxReach(shells.layers()))
                .orElse(GATHER_SKIN_REACH);
    }

    private record SkinLimbKey(boolean slim, String name, boolean outerLayer) {
    }

    private record Impact(long startMillis, int seed) {
    }

    /**
     * One player's last reaction of each kind on each arm, with what they were doing last tick to spot a new one.
     */
    private static final class Reactions {
        private static final int EITHER_ARM = -1;

        // [Reaction.ordinal()][0 for the right arm, 1 for the left]
        private final Impact[][] last = new Impact[QiCoatingDefinition.Reaction.values().length][2];
        private boolean swinging;
        private int hurtTime;

        private Reactions(boolean swinging, int hurtTime) {
            this.swinging = swinging;
            this.hurtTime = hurtTime;
        }

        private void record(QiCoatingDefinition.Reaction reaction, int arm, Impact impact) {
            last[reaction.ordinal()][arm] = impact;
        }

        /**
         * The latest reaction the coating reacts to on one arm, or on either arm (EITHER_ARM). Null if there was none.
         */
        private Impact latest(QiCoating coating, int arm) {
            Impact latest = null;
            for (QiCoatingDefinition.Reaction reaction : QiCoatingDefinition.Reaction.values()) {
                if (!coating.reactsTo(reaction)) {
                    continue;
                }
                for (int side = 0; side < 2; side++) {
                    Impact impact = last[reaction.ordinal()][side];
                    if ((arm == EITHER_ARM || arm == side) && impact != null
                            && (latest == null || impact.startMillis() > latest.startMillis())) {
                        latest = impact;
                    }
                }
            }
            return latest;
        }
    }

    /**
     * Feeds a skin-fitted limb to qi_coating_skin.fsh: moves each vertex SKIN_COAT_LIFT out from the limb's middle on every axis,
     * and swaps the vertex colour for the coating's data (r: top bit 0 for a hand coating or 1 for a whole-body layer, low bits the last hit's
     * seed; g: freeze progress; b: how far up the limb, 0 at the hand or foot; a: how long ago the last hit was).
     */
    private static final class SkinCoatingConsumer extends DelegatingVertexConsumer {
        private final Matrix4f limbToWorld;
        private final Matrix4f worldToLimb;
        private final Vector3f scratch = new Vector3f();
        private final int coating;
        private final int progress;
        private final int impactAge;
        private final float centerX;
        private final float tipY;
        // A PATCHES gather (QiCoatingGather): each face is held until its four vertices are in, then split into skin pixels and
        // only those round a landing are passed on.
        private final ShellGeometry.Patches patches;
        // Per held vertex: local x, y, z (pixels), u, v, overlay, light, normal x, y, z.
        private final float[][] held = new float[4][10];
        private int heldCount;

        private SkinCoatingConsumer(VertexConsumer delegate, Matrix4fc limbToWorld, boolean mantle, float progress, int impact,
                                    float centerX, float tipY, ShellGeometry.Patches patches) {
            super(delegate);
            this.limbToWorld = new Matrix4f(limbToWorld);
            this.worldToLimb = new Matrix4f(limbToWorld).invert();
            this.coating = (mantle ? 128 : 0) | (impact & 0x7F);
            this.impactAge = impact >> 8 & 0xFF;
            this.progress = Math.round(progress * 255.0F);
            this.centerX = centerX;
            this.tipY = tipY;
            this.patches = patches;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            // ModelPart vertices arrive already transformed; undo that to work in the limb's own pixels.
            Vector3f local = worldToLimb.transformPosition(x, y, z, scratch).mul(16.0F);
            if (patches == null) {
                emit(local.x, local.y, local.z, u, v, overlayCoords, lightCoords, nx, ny, nz);
                return;
            }
            float[] vertex = held[heldCount];
            vertex[0] = local.x;
            vertex[1] = local.y;
            vertex[2] = local.z;
            vertex[3] = u;
            vertex[4] = v;
            vertex[5] = Float.intBitsToFloat(overlayCoords);
            vertex[6] = Float.intBitsToFloat(lightCoords);
            vertex[7] = nx;
            vertex[8] = ny;
            vertex[9] = nz;
            if (++heldCount == 4) {
                heldCount = 0;
                emitPatched();
            }
        }

        /**
         * The held face split into one cell per skin pixel (by its texture's extent), passing on just the revealed cells.
         */
        private void emitPatched() {
            float[] a = held[0];
            float[] b = held[1];
            float[] c = held[2];
            float[] d = held[3];
            int cellsS = Math.max(1, Math.round((Math.abs(b[3] - a[3]) + Math.abs(b[4] - a[4])) * 64.0F));
            int cellsT = Math.max(1, Math.round((Math.abs(d[3] - a[3]) + Math.abs(d[4] - a[4])) * 64.0F));
            int overlayCoords = Float.floatToRawIntBits(a[5]);
            int lightCoords = Float.floatToRawIntBits(a[6]);
            float[] point = new float[5];
            for (int i = 0; i < cellsS; i++) {
                for (int j = 0; j < cellsT; j++) {
                    float s0 = (float) i / cellsS;
                    float s1 = (float) (i + 1) / cellsS;
                    float t0 = (float) j / cellsT;
                    float t1 = (float) (j + 1) / cellsT;
                    ShellGeometry.quadPoint(a, b, c, d, (s0 + s1) * 0.5F, (t0 + t1) * 0.5F, point);
                    if (!QiCoatingGather.revealed(patches.landings(), patches.growth(), point[0], point[1], point[2],
                            QiCoatingGather.pixelJitter(point[0], point[1], point[2]))) {
                        continue;
                    }
                    float[][] corners = {{s0, t0}, {s1, t0}, {s1, t1}, {s0, t1}};
                    for (float[] corner : corners) {
                        ShellGeometry.quadPoint(a, b, c, d, corner[0], corner[1], point);
                        emit(point[0], point[1], point[2], point[3], point[4], overlayCoords, lightCoords, a[7], a[8], a[9]);
                    }
                }
            }
        }

        /**
         * Passes on one vertex given in the limb's own pixels, lifted off the limb and carrying the coating's data. Measured in
         * 12px limb lengths from the hand or foot (tipY) up, centred on centerX across and 0 front to back.
         */
        private void emit(float x, float y, float z, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            Vector3f local = scratch.set(x, y, z);
            float up = Mth.clamp((tipY - local.y) / 12.0F, 0.0F, 1.0F);
            local.add(Math.signum(local.x - centerX) * SKIN_COAT_LIFT, Math.signum(local.y - (tipY - 6.0F)) * SKIN_COAT_LIFT,
                    Math.signum(local.z) * SKIN_COAT_LIFT);
            Vector3f lifted = limbToWorld.transformPosition(local.div(16.0F));
            int data = ARGB.color(impactAge, coating, progress, Math.round(up * 255.0F));
            delegate.addVertex(lifted.x, lifted.y, lifted.z, data, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }

    }
}
