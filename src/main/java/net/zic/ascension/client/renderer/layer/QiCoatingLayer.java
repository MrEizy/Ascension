package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.Vec3;
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
import net.zic.ascension.client.renderer.ModRenderTypes;
import net.zic.ascension.client.renderer.QiCoatingLooks;
import net.zic.ascension.client.visual.QiCoatingClientState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

/**
 * Draws players' Qi Coatings (QiCoatingDefinition, loaded from datapacks; the looks they name are in QiCoatingLooks): the shell
 * looks' boxes round the arms or every part, the skin looks painted into the player's own skin pixels, the fading and echoing
 * arms, the overlay coating, hand particles and the coded effects (QiCoatingEffects). Geometry is built in each part's local
 * model space, so it follows every animation, in third and first person.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public class QiCoatingLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    // Arm cubes run from y = -2 (shoulder) to y = 10 (hand tip), 12px long. A quarter is 3px.
    private static final float HAND_TIP_Y = 10.0F;
    private static final float SHOULDER_Y = -2.0F;
    // Whole-body echoes are scattered further than hand echoes so they read at body scale.
    private static final float BODY_ECHO_SPREAD = 1.5F;
    private static final float ARM_MIN_Z = -2.0F;
    private static final float ARM_MAX_Z = 2.0F;
    // How much of the shader's v range a box cap spans across its depth, so its pattern matches the faces next to it.
    private static final float CAP_V_SPAN = 0.15F;
    // Stone plates: how far around the limb (as a share of the perimeter) each limb's pattern starts, for the right arm, left
    // arm, right leg and left leg. Other per-limb patterns (water streams, pixel lightning) leave u unshifted so the shader knows
    // each limb's outer side, and get their own shader style per limb instead (QiCoatingLooks.ShellLook.limbStyle). The pattern wraps, so
    // this gives each limb its own layout. Added to the style offset, it stays inside the style's u range of 2, and the shader
    // wraps it back onto the perimeter.
    private static final float[] LIMB_PATTERN_SHIFTS = {0.0F, 0.37F, 0.61F, 0.18F};
    // Lightning Step's ground effects (footprints and the spear flash trail) are drawn as crackling bolts (shader style 51 for
    // the trail) lifted TRAIL_LIFT off the ground; feet sit TRAIL_FOOT_SIDE blocks either side of the player's centre.
    private static final float TRAIL_STYLE_OFFSET = 51.0F * 2.0F;
    private static final double TRAIL_LIFT = 0.02D;
    private static final double TRAIL_FOOT_SIDE = 0.12D;
    // Lightning Step (QiCoatingEffects.LIGHTNING_STEP) footprints: a crackling mark (shader styles 52-55) put down every MARK_SPACING blocks walked on the
    // ground, alternating feet, and under both feet on landing after MARK_LANDING_AIR_TICKS or more in the air. Each lasts
    // MARK_TICKS, fading over the last MARK_FADE_TICKS, MARK_SIZE blocks across (MARK_LANDING_SIZE on landing), lifted
    // TRAIL_LIFT off the ground.
    private static final float MARK_STYLE_OFFSET = 52.0F * 2.0F;
    private static final int MARK_VARIANTS = 4;
    private static final int MARK_TICKS = 60;
    private static final int MARK_FADE_TICKS = 20;
    private static final double MARK_SPACING = 0.7D;
    private static final double MARK_SIZE = 0.25D;
    private static final double MARK_LANDING_SIZE = 0.25D;
    private static final int MARK_LANDING_AIR_TICKS = 4;
    private static final Map<Integer, FootMarks> FOOT_MARKS = new HashMap<>();
    // Lightning Step spear flash (started by the server, QiCoatingService): the player is driven forward at FLASH_SPEED blocks a
    // tick for FLASH_TICKS ticks, then keeps FLASH_KEEP of that speed to slide out of it. A crossed bolt ribbon (style 51)
    // FLASH_TRAIL_WIDTH wide runs from each foot (TRAIL_FOOT_SIDE either side of their path), FLASH_TRAIL_HEIGHT above the ground,
    // and stays for FLASH_TRAIL_TICKS after, fading over the last FLASH_TRAIL_FADE_TICKS.
    private static final int FLASH_TICKS = 4;
    private static final double FLASH_SPEED = 1.75D;
    private static final double FLASH_KEEP = 0.4D;
    private static final int FLASH_TRAIL_TICKS = 14;
    private static final int FLASH_TRAIL_FADE_TICKS = 14;
    private static final double FLASH_TRAIL_WIDTH = 0.3D;
    private static final double FLASH_TRAIL_HEIGHT = 0.15D;
    // Your own flash: direction, and ticks of drive left (the last one is the slide-out).
    private static Vec3 flashDirection = Vec3.ZERO;
    private static int flashTicksLeft;
    private static final Map<Integer, List<FlashTrail>> FLASH_TRAILS = new HashMap<>();
    // Serpent Coil splash at the top of each limb: must match SERPENT_PERIOD, SERPENT_ACTIVE, SERPENT_TOP, SERPENT_LENGTH and
    // SERPENT_LIMB_OFFSETS in qi_coating.fsh, so the spray comes as the serpent's head thins away near the shoulder or hip.
    private static final double SERPENT_PERIOD_SECONDS = 4.0D;
    private static final double SERPENT_ACTIVE = 0.85D;
    private static final double SERPENT_BURST_PROGRESS = (0.88D + 0.05D) / (0.88D + 0.4D + 0.05D);
    private static final double[] SERPENT_LIMB_OFFSETS = {0.0D, 0.43D, 0.71D, 0.19D};
    private static final int SERPENT_SPLASH_PARTICLES = 5;
    // Ice and metal coatings freeze over (or spread over) from the hand or foot when they come on, and shrink back down to it
    // when they go off, taking QiCoating.growMillis each way. How far along they are goes to qi_coating_skin.fsh in the
    // vertex green channel (SkinCoatingConsumer).
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
    // Tornado Fist (submitWindRings): a box of WIND_RING_COUNT stacked wind rings hugging the forearm, from WIND_RING_TIP_OVERHANG
    // past the hand tip toward the elbow, standing still while the wind streaks run round them (qi_coating.fsh). Each ring sits
    // WIND_RING_OUTSET out from the arm and is WIND_RING_HEIGHT tall with WIND_RING_GAP between rings (all in pixels); the bottom
    // one is WIND_RING_ALPHA opaque and each one above WIND_RING_ALPHA_FALLOFF fainter.
    private static final int WIND_RING_COUNT = 5;
    private static final float WIND_RING_TIP_OVERHANG = 0.3F;
    private static final float WIND_RING_OUTSET = 0.5F;
    private static final float WIND_RING_HEIGHT = 0.9F;
    private static final float WIND_RING_GAP = 0.5F;
    private static final float WIND_RING_ALPHA = 0.9F;
    private static final float WIND_RING_ALPHA_FALLOFF = 0.12F;
    // The qi_coating.fsh style that shades the wind rings.
    private static final int WIND_RING_STYLE = 32;
    // How far around the ring each one's streak pattern starts, so stacked rings don't repeat each other (see LIMB_PATTERN_SHIFTS).
    private static final float RING_PATTERN_SHIFT = 0.37F;

    // Coatings that fade the arm (fades_arm) swap it for a copy that is opaque at the shoulder and fades toward the hand.
    private static final float GHOST_HAND_ALPHA = 0.25F;

    // Coatings that echo: faint violet copies of the arm, re-scattered every ECHO_STEP_MILLIS, up to ECHO_SPREAD pixels away.
    private static final int ECHO_COUNT = 2;
    private static final long ECHO_STEP_MILLIS = 90L;
    private static final float ECHO_SPREAD = 0.9F;
    private static final int ECHO_TINT = 0x59B98CFF;
    // Coatings that echo and react to something (reacts_to) only echo for ECHO_TRIGGER_MILLIS after it happens.
    private static final long ECHO_TRIGGER_MILLIS = 700L;

    // Lightning Step: the player's own model is drawn at this opacity while active.
    public static final float STEP_BODY_ALPHA = 0.9F;
    // PlayerRenderer scales the model by this before drawing it.
    private static final float PLAYER_MODEL_SCALE = 0.9375F;

    public QiCoatingLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
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
        QiCoating coating = QiCoatingClientState.get(player.getUUID(), QiCoatingChannel.BODY);
        if (coating == null || coating.swordQi()) {
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
        if (coating.fadesArm() || coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP)) {
            // Replace vanilla's opaque hand with a fading or translucent one.
            event.setCanceled(true);
        }
        PlayerModelPart sleevePart = event.getArm() == HumanoidArm.RIGHT ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE;
        submitCoatedArm(poseStack, event.getSubmitNodeCollector(), coating, player.getSkin().body().texturePath(), event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, event.getArm(), slim, player.isModelPartShown(sleevePart), player.getId());
        poseStack.popPose();
    }

    /**
     * Per-tick work for each player's coating: its grow clock, its hand particles, the serpent splashes, hit reactions and
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
            List<QiCoating> shownCoatings = new ArrayList<>();
            for (QiCoatingChannel channel : QiCoatingChannel.values()) {
                QiCoating coating = QiCoatingClientState.get(player.getUUID(), channel);
                QiCoatingClientState.trackActivation(player.getUUID(), channel, QiCoatingClientState.live(player.getUUID(), channel));
                if (coating != null && !player.isInvisible()) {
                    shownCoatings.add(coating);
                }
            }
            for (QiCoating coating : shownCoatings) {
                for (QiCoatingDefinition.HandParticles particles : coating.definition().handParticles()) {
                    forEachHand(minecraft, player, hand -> spawnHandParticles(level, hand, particles));
                }
            }
            shownCoatings.stream().filter(coating -> coating.hasEffect(QiCoatingEffects.SERPENT_SPLASHES)).findFirst().ifPresent(splashing ->
                    spawnSerpentSplashes(minecraft, level, player, splashing.wholeBody() ? SERPENT_LIMB_OFFSETS.length : 2));
            if (shownCoatings.stream().anyMatch(QiCoating::reacts)) {
                watchReactions(player);
            } else {
                REACTIONS.remove(player.getId());
            }
            if (shownCoatings.stream().anyMatch(coating -> coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP))) {
                recordFootMarks(level, player);
            } else {
                FOOT_MARKS.remove(player.getId());
            }
        }
        // Players who left the level.
        FOOT_MARKS.keySet().removeIf(id -> level.getEntity(id) == null);
        REACTIONS.keySet().removeIf(id -> level.getEntity(id) == null);
        recordFlashTrails(level);
    }

    /**
     * Lightning Step spear flash, from LightningFlashPacket: drives your own player forward if it is you, and starts recording
     * the flasher's path for the trail on every client, with a footprint burst under both feet.
     */
    public static void startLightningFlash(int entityId, float dirX, float dirZ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Entity entity = level == null ? null : level.getEntity(entityId);
        if (entity == null) {
            return;
        }
        if (entity == minecraft.player) {
            flashDirection = new Vec3(dirX, 0.0D, dirZ);
            flashTicksLeft = FLASH_TICKS + 1;
        }
        FlashTrail trail = new FlashTrail();
        trail.points.add(entity.position().add(0.0D, FLASH_TRAIL_HEIGHT, 0.0D));
        trail.recordTicksLeft = FLASH_TICKS + 1;
        FLASH_TRAILS.computeIfAbsent(entityId, _ -> new ArrayList<>()).add(trail);
        burstFootMarks(level, entity);
    }

    /**
     * Your own flash's drive, set before the player moves each tick.
     */
    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (flashTicksLeft <= 0 || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        double speed = flashTicksLeft > 1 ? FLASH_SPEED : FLASH_SPEED * FLASH_KEEP;
        minecraft.player.setDeltaMovement(flashDirection.x * speed, minecraft.player.getDeltaMovement().y, flashDirection.z * speed);
        flashTicksLeft--;
    }

    /**
     * Adds each flashing player's position to their trail while the flash lasts, puts a footprint burst down where it ends, and
     * drops trails once they have faded.
     */
    private static void recordFlashTrails(ClientLevel level) {
        long now = level.getGameTime();
        FLASH_TRAILS.entrySet().removeIf(entry -> {
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null) {
                return true;
            }
            entry.getValue().removeIf(trail -> {
                if (trail.recordTicksLeft > 0) {
                    trail.points.add(entity.position().add(0.0D, FLASH_TRAIL_HEIGHT, 0.0D));
                    if (--trail.recordTicksLeft == 0) {
                        trail.endTick = now;
                        burstFootMarks(level, entity);
                    }
                    return false;
                }
                return now - trail.endTick > FLASH_TRAIL_TICKS;
            });
            return entry.getValue().isEmpty();
        });
    }

    /**
     * A large footprint under both of the entity's feet, like landing from a jump.
     */
    private static void burstFootMarks(ClientLevel level, Entity entity) {
        FootMarks marks = FOOT_MARKS.computeIfAbsent(entity.getId(), _ -> new FootMarks());
        RandomSource random = level.getRandom();
        float bodyRot = entity instanceof AbstractClientPlayer player ? player.yBodyRot : entity.getYRot();
        for (Vec3 foot : footPositions(entity.position(), bodyRot)) {
            marks.marks.add(new FootMark(foot, bodyRot * Mth.DEG_TO_RAD, level.getGameTime(), random.nextInt(MARK_VARIANTS),
                    MARK_LANDING_SIZE));
        }
    }

    /**
     * A coating's hand particles at one hand this tick: `count` of them, `chance` of the time. Converging ones start at the hand
     * with a velocity of `converge` in a random direction, for particles (like portal ones) that travel from their start plus
     * velocity back to their start, so they stream into the hand.
     */
    private static void spawnHandParticles(ClientLevel level, Vec3 hand, QiCoatingDefinition.HandParticles particles) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= particles.chance()) {
            return;
        }
        for (int i = 0; i < particles.count(); i++) {
            Vec3 velocity = particles.velocity();
            if (particles.converge() > 0.0F) {
                velocity = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize()
                        .scale(particles.converge());
            }
            level.addParticle(particles.particle(), hand.x + random.triangle(0.0D, particles.spread()), hand.y + particles.offsetY(),
                    hand.z + random.triangle(0.0D, particles.spread()), velocity.x, velocity.y, velocity.z);
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
     * Serpent Coil: a burst of spray at the top of each limb on the tick its serpent's head reaches it, timed the way
     * qi_coating.fsh times the serpents (GameTime runs on game ticks, wrapping every 24000). Positions are the limb tops in a
     * standing pose. Skipped for your own player in first person, where the limb tops are off screen.
     *
     * @param limbs how many limbs, in SERPENT_LIMB_OFFSETS order, carry a serpent: 2 for the arms only, 4 for arms and legs
     */
    private static void spawnSerpentSplashes(Minecraft minecraft, ClientLevel level, AbstractClientPlayer player, int limbs) {
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        double seconds = (level.getGameTime() % 24000L) / 20.0D;
        double previous = ((level.getGameTime() - 1L) % 24000L) / 20.0D;
        double burst = SERPENT_BURST_PROGRESS * SERPENT_ACTIVE;
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw));
        RandomSource random = level.getRandom();
        for (int limb = 0; limb < limbs; limb++) {
            double cycle = Mth.frac(seconds / SERPENT_PERIOD_SECONDS + SERPENT_LIMB_OFFSETS[limb]);
            double before = Mth.frac(previous / SERPENT_PERIOD_SECONDS + SERPENT_LIMB_OFFSETS[limb]);
            if (!(before < burst && cycle >= burst)) {
                continue;
            }
            // Limbs in SERPENT_LIMB_OFFSETS order: right arm, left arm, right leg, left leg.
            boolean arm = limb < 2;
            double sideSign = limb % 2 == 0 ? 1.0D : -1.0D;
            Vec3 top = player.position().add(right.scale((arm ? 0.36D : 0.12D) * sideSign))
                    .add(0.0D, (arm ? 1.3D : 0.65D) * player.getScale(), 0.0D);
            for (int i = 0; i < SERPENT_SPLASH_PARTICLES; i++) {
                level.addParticle(ParticleTypes.SPLASH, top.x + random.triangle(0.0D, 0.12D), top.y,
                        top.z + random.triangle(0.0D, 0.12D), random.triangle(0.0D, 0.1D), 0.1D, random.triangle(0.0D, 0.1D));
            }
        }
    }

    private static void forEachHand(Minecraft minecraft, AbstractClientPlayer player, Consumer<Vec3> action) {
        boolean firstPerson = player == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
        if (firstPerson) {
            // Only the main arm is drawn in first person, low on the screen to its side.
            action.accept(firstPersonHand(player));
        } else {
            action.accept(thirdPersonHand(player, HumanoidArm.RIGHT));
            action.accept(thirdPersonHand(player, HumanoidArm.LEFT));
        }
    }

    private static Vec3 thirdPersonHand(AbstractClientPlayer player, HumanoidArm side) {
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw));
        double sideSign = side == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        return player.position().add(right.scale(0.36D * sideSign)).add(0.0D, 0.68D * player.getScale(), 0.0D);
    }

    private static Vec3 firstPersonHand(AbstractClientPlayer player) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        Vec3 up = right.cross(look).normalize();
        double sideSign = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        return player.getEyePosition().add(look.scale(0.55D)).add(right.scale(0.32D * sideSign)).add(up.scale(-0.3D));
    }

    /**
     * Called from LivingEntityRendererQiCoatingMixin: Lightning Step draws the player's own model translucent.
     */
    public static boolean drawsTranslucentBody(AvatarRenderState state) {
        if (state.isInvisible) {
            return false;
        }
        QiCoating coating = coatingFor(state);
        return coating != null && coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP);
    }

    /**
     * Called from PlayerModelQiCoatingMixin to decide whether the vanilla arms should be hidden.
     */
    public static boolean drawsGhostArms(AvatarRenderState state) {
        if (state.isInvisible || state.isSpectator) {
            return false;
        }
        QiCoating coating = coatingFor(state);
        return coating != null && coating.fadesArm();
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        QiCoating coating = coatingFor(state);
        if (coating == null || coating.swordQi()) {
            return;
        }
        boolean slim = state.skin.model() == PlayerModelType.SLIM;
        if (coating.wholeBody()) {
            submitBodyLayer(poseStack, submitNodeCollector, lightCoords, state, coating, slim);
            return;
        }
        submitArm(poseStack, submitNodeCollector, lightCoords, state, coating, HumanoidArm.RIGHT, slim);
        submitArm(poseStack, submitNodeCollector, lightCoords, state, coating, HumanoidArm.LEFT, slim);
    }

    /**
     * Qi Layer in third person: the skin look on the limbs, the shells over the body (Shells.body), the overlay coating's on top,
     * then Lightning Step's ground effects and (for echo layers) faint copies of the whole model scattered around it.
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
        coating.definition().overlay().map(QiCoatingClientState::byId)
                .ifPresent(overlay -> submitBodyShells(poseStack, submitNodeCollector, model, overlay, slim, lightCoords, growth, false));
        if (coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP)) {
            submitFootMarks(poseStack, submitNodeCollector, state);
            submitFlashTrail(poseStack, submitNodeCollector, state);
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
            submitFilmShells(poseStack, submitNodeCollector, model, look, body, slim);
        }
    }

    /**
     * A film over every body part (or, with a limb_reach, just up that far from each hand and foot, with open tops).
     */
    private static void submitFilmShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                         QiCoatingLooks.ShellLook look, QiCoatingDefinition.Body body, boolean slim) {
        RenderType renderType = renderType(look);
        float styleOffset = look.style() * 2.0F;
        float armTop = body.limbReach().map(reach -> HAND_TIP_Y - reach).orElse(SHOULDER_Y);
        float legTop = body.limbReach().map(reach -> 12.0F - reach).orElse(0.0F);
        boolean limbCaps = body.limbReach().isEmpty();
        float inflate = body.inflate();
        // Cube bounds from HumanoidModel/PlayerModel.createMesh, in each part's local space.
        float armWidth = slim ? 3.0F : 4.0F;
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        if (body.torsoAndHead()) {
            submitPartShell(poseStack, submitNodeCollector, model.head, renderType, styleOffset,
                    -4.0F, 4.0F, -8.0F, 0.0F, -4.0F, 4.0F, true, inflate);
            submitPartShell(poseStack, submitNodeCollector, model.body, renderType, styleOffset,
                    -4.0F, 4.0F, 0.0F, 12.0F, -2.0F, 2.0F, true, inflate);
        }
        submitPartShell(poseStack, submitNodeCollector, model.rightArm, renderType, styleOffset,
                1.0F - armWidth, 1.0F, armTop, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, limbCaps, inflate);
        submitPartShell(poseStack, submitNodeCollector, model.leftArm, renderType, styleOffset,
                -1.0F, armWidth - 1.0F, armTop, HAND_TIP_Y, ARM_MIN_Z, ARM_MAX_Z, limbCaps, inflate);
        submitPartShell(poseStack, submitNodeCollector, model.rightLeg, renderType, styleOffset,
                -2.0F, 2.0F, legTop, 12.0F, -2.0F, 2.0F, limbCaps, inflate);
        submitPartShell(poseStack, submitNodeCollector, model.leftLeg, renderType, styleOffset,
                -2.0F, 2.0F, legTop, 12.0F, -2.0F, 2.0F, limbCaps, inflate);
        poseStack.popPose();
    }

    /**
     * A box round each arm and leg (or just each leg) with the look's per-limb pattern, such as the Stone Mantle's plates or
     * the Serpent Coil's serpents. Each limb gets its own style or starting point round the limb (limbStyleOffset), so no two
     * limbs repeat.
     */
    private static void submitLimbPatternShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                                                QiCoatingLooks.ShellLook look, QiCoatingDefinition.Body body, boolean slim) {
        RenderType renderType = renderType(look);
        float inflate = body.inflate();
        float armWidth = slim ? 3.0F : 4.0F;
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

    /**
     * The middle of an arm's cube across its width, in its local pixels (from PlayerModel.createMesh).
     */
    private static float armCenterX(HumanoidArm side, boolean slim) {
        float center = slim ? 0.5F : 1.0F;
        return side == HumanoidArm.RIGHT ? -center : center;
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
        RenderPipeline pipeline = coating.definition().skin().map(look -> QiCoatingLooks.skin(look.look())).orElse(null);
        if (pipeline == null) {
            return;
        }
        boolean wholeBody = coating.wholeBody();
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.qiCoatingSkin(pipeline, skin), (pose, buffer) -> {
            PoseStack limbPose = new PoseStack();
            limbPose.last().set(pose);
            limb.render(limbPose, new SkinCoatingConsumer(buffer, pose.pose(), wholeBody, progress, impact, centerX, tipY),
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

    /**
     * The Tornado Fist's box of wind rings (QiCoatingEffects.WIND_RINGS) round one arm: WIND_RING_COUNT rectangular rings stacked
     * from the hand tip toward the elbow, each a band of four upright faces shaded by qi_coating.fsh's wind rings
     * (WIND_RING_STYLE). Expects the pose stack to already be in the arm's local space. u runs around each ring and v up its band.
     */
    private static void submitWindRings(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm side, boolean slim) {
        float styleOffset = WIND_RING_STYLE * 2.0F;
        float centerX = armCenterX(side, slim);
        float halfX = (slim ? 3.0F : 4.0F) * 0.5F + WIND_RING_OUTSET;
        float halfZ = (ARM_MAX_Z - ARM_MIN_Z) * 0.5F + WIND_RING_OUTSET;
        float x0 = (centerX - halfX) / 16.0F;
        float x1 = (centerX + halfX) / 16.0F;
        float z0 = -halfZ / 16.0F;
        float z1 = halfZ / 16.0F;
        float[] cornerX = {x0, x1, x1, x0};
        float[] cornerZ = {z0, z0, z1, z1};
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
            for (int ring = 0; ring < WIND_RING_COUNT; ring++) {
                float bottom = HAND_TIP_Y + WIND_RING_TIP_OVERHANG - ring * (WIND_RING_HEIGHT + WIND_RING_GAP);
                float top = bottom - WIND_RING_HEIGHT;
                float shift = Mth.frac(ring * RING_PATTERN_SHIFT);
                int color = ARGB.white(WIND_RING_ALPHA * (1.0F - ring * WIND_RING_ALPHA_FALLOFF));
                for (int face = 0; face < 4; face++) {
                    int next = (face + 1) % 4;
                    side(pose, buffer, cornerX[face], cornerZ[face], cornerX[next], cornerZ[next],
                            styleOffset + shift + face * 0.25F, styleOffset + shift + (face + 1) * 0.25F, top / 16.0F, bottom / 16.0F, color);
                }
            }
        });
    }

    /**
     * The u style offset for one limb (right arm, left arm, right leg, left leg) of a per-limb patterned look: its shader style
     * for that limb (ShellLook.limbStyle), plus (for stone plates) that limb's LIMB_PATTERN_SHIFTS.
     */
    private static float limbStyleOffset(QiCoatingLooks.ShellLook look, int limb) {
        return look.limbStyle(limb) * 2.0F + (look.stone() ? LIMB_PATTERN_SHIFTS[limb] : 0.0F);
    }

    private static void submitPartShell(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPart part, RenderType renderType,
                                        float styleOffset, float minX, float maxX, float minY, float maxY, float minZ, float maxZ,
                                        boolean topCap, float inflate) {
        submitPartShell(poseStack, submitNodeCollector, part, renderType, styleOffset, minX, maxX, minY, maxY, minZ, maxZ,
                topCap, inflate, false);
    }

    /**
     * @param squareCap whether the caps are mapped so a square texel grid on the sides stays square on them, instead of CAP_V_SPAN
     */
    private static void submitPartShell(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPart part, RenderType renderType,
                                        float styleOffset, float minX, float maxX, float minY, float maxY, float minZ, float maxZ,
                                        boolean topCap, float inflate, boolean squareCap) {
        if (!part.visible) {
            return;
        }
        float capVSpan = squareCap ? (maxZ - minZ + 2.0F * inflate) / (maxY - minY + 2.0F * inflate) : CAP_V_SPAN;
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) ->
                emitBox(pose, buffer, minX - inflate, maxX + inflate, minY - inflate, maxY + inflate, minZ - inflate, maxZ + inflate,
                        styleOffset, 0xFFFFFFFF, topCap, capVSpan));
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
        // Checked via state rather than arm.visible, which PlayerModelQiCoatingMixin clears for void coatings.
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
     * wind rings, the fading arm, the shells, their echoes, the overlay coating's shells, and Lightning Step's see-through arm.
     * Expects the pose stack to already be in the arm's local space.
     */
    private static void submitCoatedArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating,
                                        Identifier skinTexture, int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve, int seed) {
        if (coating.definition().skin().isPresent()) {
            submitSkinLimb(poseStack, submitNodeCollector, skinTexture, coating, freezeProgress(coating, seed),
                    impactData(coating, seed, side == HumanoidArm.RIGHT ? 0 : 1), lightCoords,
                    skinLimb(slim, side == HumanoidArm.RIGHT ? "right_arm" : "left_arm", sleeve), armCenterX(side, slim), HAND_TIP_Y);
        }
        if (coating.hasEffect(QiCoatingEffects.WIND_RINGS)) {
            submitWindRings(poseStack, submitNodeCollector, side, slim);
        }
        if (coating.fadesArm()) {
            submitGhostArm(poseStack, submitNodeCollector, skinTexture, lightCoords, overlayCoords, side, slim, sleeve, 0xFFFFFFFF, GHOST_HAND_ALPHA);
        }
        float growth = freezeProgress(coating, seed);
        boolean echoing = echoing(coating, seed);
        submitShells(poseStack, submitNodeCollector, coating, side, slim, lightCoords, growth, !echoing);
        if (echoing) {
            submitEchoes(poseStack, submitNodeCollector, coating, skinTexture, lightCoords, overlayCoords, side, slim, sleeve, growth);
        }
        coating.definition().overlay().map(QiCoatingClientState::byId)
                .ifPresent(overlay -> submitShells(poseStack, submitNodeCollector, overlay, side, slim, lightCoords, growth, false));
        if (coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP)) {
            // In first person the body layer isn't drawn, so the arm itself goes see-through here.
            submitGhostArm(poseStack, submitNodeCollector, skinTexture, lightCoords, overlayCoords, side, slim, sleeve,
                    ARGB.white(STEP_BODY_ALPHA), 1.0F);
        }
    }

    /**
     * Lightning Step: drops expired footprints and puts down new ones as the player walks or lands, for submitFootMarks.
     */
    private static void recordFootMarks(ClientLevel level, AbstractClientPlayer player) {
        FootMarks state = FOOT_MARKS.computeIfAbsent(player.getId(), _ -> new FootMarks());
        long now = level.getGameTime();
        state.marks.removeIf(mark -> now - mark.spawnTick() > MARK_TICKS);
        RandomSource random = level.getRandom();
        Vec3 position = player.position();
        Vec3[] feet = footPositions(position, player.yBodyRot);
        if (player.onGround()) {
            if (state.airTicks >= MARK_LANDING_AIR_TICKS) {
                for (Vec3 foot : feet) {
                    state.marks.add(new FootMark(foot, player.yBodyRot * Mth.DEG_TO_RAD, now, random.nextInt(MARK_VARIANTS), MARK_LANDING_SIZE));
                }
                state.lastMark = position;
            } else if (state.lastMark == null || position.distanceTo(state.lastMark) >= MARK_SPACING) {
                state.marks.add(new FootMark(feet[state.nextFoot], player.yBodyRot * Mth.DEG_TO_RAD, now,
                        random.nextInt(MARK_VARIANTS), MARK_SIZE));
                state.nextFoot = 1 - state.nextFoot;
                state.lastMark = position;
            }
            state.airTicks = 0;
        } else {
            state.airTicks++;
        }
    }

    /**
     * Lightning Step: each footprint as a flat square on the ground, turned to its own angle, its vertex alpha fading over
     * its life (the shader also breaks it up as it fades). Built in world space around the player, then turned into
     * the model space the layer draws in by undoing the renderer's body rotation, flip, scale and 1.501 drop (trailVertex).
     */
    private static void submitFootMarks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state) {
        FootMarks marks = FOOT_MARKS.get(state.id);
        Minecraft minecraft = Minecraft.getInstance();
        if (marks == null || marks.marks.isEmpty() || minecraft.level == null) {
            return;
        }
        float time = minecraft.level.getGameTime() + state.partialTick;
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        Quaternionf unrotate = Axis.YP.rotationDegrees(-(180.0F - state.bodyRot));
        float scale = PLAYER_MODEL_SCALE * state.scale;
        List<FootMark> current = List.copyOf(marks.marks);
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
            for (FootMark mark : current) {
                float life = Math.min(1.0F, (mark.spawnTick() + MARK_TICKS - time) / MARK_FADE_TICKS);
                if (life <= 0.0F) {
                    continue;
                }
                int color = ARGB.white(Math.min(1.0F, life));
                float style = MARK_STYLE_OFFSET + mark.variant() * 2.0F;
                Vec3 x = new Vec3(Mth.cos(mark.angle()), 0.0D, Mth.sin(mark.angle())).scale(mark.size() * 0.5D);
                Vec3 z = new Vec3(-x.z, 0.0D, x.x);
                Vec3 centre = mark.position();
                trailVertex(pose, buffer, centre.subtract(x).subtract(z), origin, unrotate, scale, style, 0.0F, color);
                trailVertex(pose, buffer, centre.add(x).subtract(z), origin, unrotate, scale, style + 1.0F, 0.0F, color);
                trailVertex(pose, buffer, centre.add(x).add(z), origin, unrotate, scale, style + 1.0F, 1.0F, color);
                trailVertex(pose, buffer, centre.subtract(x).add(z), origin, unrotate, scale, style, 1.0F, color);
            }
        });
    }

    /**
     * Lightning Step spear flash: a jagged bolt along the path the player just flashed down, as a flat and an upright ribbon
     * crossed so it reads from any angle; it stays crackling for FLASH_TRAIL_TICKS after the flash, fading over the last
     * FLASH_TRAIL_FADE_TICKS. u runs from the player's end (0) back to the start (1), where the bolt breaks up. Each of the
     * player's recent flashes keeps its own trail.
     */
    private static void submitFlashTrail(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state) {
        List<FlashTrail> trails = FLASH_TRAILS.get(state.id);
        if (trails != null) {
            for (FlashTrail trail : List.copyOf(trails)) {
                submitFlashTrail(poseStack, submitNodeCollector, state, trail);
            }
        }
    }

    private static void submitFlashTrail(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state,
                                         FlashTrail trail) {
        Minecraft minecraft = Minecraft.getInstance();
        if (trail.points.size() < 2 || minecraft.level == null) {
            return;
        }
        float life = trail.recordTicksLeft > 0 ? 1.0F
                : Math.min(1.0F, (trail.endTick + FLASH_TRAIL_TICKS - minecraft.level.getGameTime() - state.partialTick)
                / FLASH_TRAIL_FADE_TICKS);
        if (life <= 0.0F) {
            return;
        }
        List<Vec3> points = new ArrayList<>(trail.points);
        Collections.reverse(points);
        double total = 0.0D;
        for (int i = 1; i < points.size(); i++) {
            total += points.get(i).distanceTo(points.get(i - 1));
        }
        if (total < 0.1D) {
            return;
        }
        int color = ARGB.white(Math.min(1.0F, life));
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        Quaternionf unrotate = Axis.YP.rotationDegrees(-(180.0F - state.bodyRot));
        float scale = PLAYER_MODEL_SCALE * state.scale;
        double length = total;
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
            double travelled = 0.0D;
            for (int i = 1; i < points.size(); i++) {
                Vec3 a = points.get(i - 1);
                Vec3 b = points.get(i);
                double segment = b.distanceTo(a);
                Vec3 flat = new Vec3(b.x - a.x, 0.0D, b.z - a.z);
                if (segment < 1.0E-3D || flat.lengthSqr() < 1.0E-6D) {
                    continue;
                }
                float u0 = TRAIL_STYLE_OFFSET + (float) (travelled / length);
                travelled += segment;
                float u1 = TRAIL_STYLE_OFFSET + (float) (travelled / length);
                Vec3 sideways = new Vec3(-flat.z, 0.0D, flat.x).normalize();
                Vec3 across = sideways.scale(FLASH_TRAIL_WIDTH * 0.5D);
                Vec3 up = new Vec3(0.0D, FLASH_TRAIL_WIDTH * 0.5D, 0.0D);
                // One trail from each foot.
                for (double footSide : new double[]{TRAIL_FOOT_SIDE, -TRAIL_FOOT_SIDE}) {
                    Vec3 footA = a.add(sideways.scale(footSide));
                    Vec3 footB = b.add(sideways.scale(footSide));
                    for (Vec3 side : new Vec3[]{across, up}) {
                        trailVertex(pose, buffer, footA.subtract(side), origin, unrotate, scale, u0, 0.0F, color);
                        trailVertex(pose, buffer, footA.add(side), origin, unrotate, scale, u0, 1.0F, color);
                        trailVertex(pose, buffer, footB.add(side), origin, unrotate, scale, u1, 1.0F, color);
                        trailVertex(pose, buffer, footB.subtract(side), origin, unrotate, scale, u1, 0.0F, color);
                    }
                }
            }
        });
    }

    /**
     * Lightning Step spear flash: the path one player flashed down, recorded tick by tick.
     */
    private static final class FlashTrail {
        private final List<Vec3> points = new ArrayList<>();
        private int recordTicksLeft;
        private long endTick;
    }

    /**
     * Lightning Step: one player's footprints and what decides the next one.
     */
    private static final class FootMarks {
        private final List<FootMark> marks = new ArrayList<>();
        private Vec3 lastMark;
        private int nextFoot;
        private int airTicks;
    }

    /**
     * @param angle   rotation of the mark on the ground, in radians (the body's, so the square lines up with the foot)
     * @param variant which of the shader's MARK_VARIANTS bolt layouts it uses
     * @param size    width of the mark in blocks
     */
    private record FootMark(Vec3 position, float angle, long spawnTick, int variant, double size) {
    }

    /**
     * World positions of the right and left foot of a player standing at `origin` facing `bodyRot`.
     */
    private static Vec3[] footPositions(Vec3 origin, float bodyRot) {
        float yaw = bodyRot * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw)).scale(TRAIL_FOOT_SIDE);
        return new Vec3[]{origin.add(right), origin.subtract(right)};
    }

    /**
     * A vertex at a world position near the player, turned into the layer's model space and lifted TRAIL_LIFT off the ground.
     */
    private static void trailVertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 world, Vec3 origin, Quaternionf unrotate,
                                    float scale, float u, float v, int color) {
        Vector3f p = unrotate.transform(new Vector3f((float) (world.x - origin.x), (float) (world.y - origin.y + TRAIL_LIFT),
                (float) (world.z - origin.z)));
        p.mul(-1.0F / scale, -1.0F / scale, 1.0F / scale).add(0.0F, 1.501F, 0.0F);
        vertex(pose, buffer, p.x, p.y, p.z, u, v, color);
    }

    /**
     * Faint violet copies of the arm and coating that jump to a new offset several times a second.
     */
    private static void submitEchoes(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating,
                                     Identifier skinTexture, int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve,
                                     float growth) {
        long step = Util.getMillis() / ECHO_STEP_MILLIS;
        for (int echo = 0; echo < ECHO_COUNT; echo++) {
            RandomSource random = RandomSource.create(step * 31L + echo * 7L + side.ordinal());
            float x = (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD;
            float y = (random.nextFloat() - 0.5F) * ECHO_SPREAD;
            float z = (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD;
            poseStack.pushPose();
            poseStack.translate(x / 16.0F, y / 16.0F, z / 16.0F);
            submitGhostArm(poseStack, submitNodeCollector, skinTexture, lightCoords, overlayCoords, side, slim, sleeve, ECHO_TINT,
                    coating.fadesArm() ? GHOST_HAND_ALPHA : 1.0F);
            submitShells(poseStack, submitNodeCollector, coating, side, slim, lightCoords, growth, false);
            poseStack.popPose();
        }
    }

    /**
     * Expects the pose stack to already be in the arm's local space.
     */
    private static void submitGhostArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier skinTexture,
                                       int lightCoords, int overlayCoords, HumanoidArm side, boolean slim, boolean sleeve, int tint, float handAlpha) {
        ModelPart arm = ghostArm(slim, side, sleeve);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(skinTexture), (pose, buffer) -> {
            PoseStack armPose = new PoseStack();
            armPose.last().set(pose);
            arm.render(armPose, new ArmFadeConsumer(buffer, pose.pose(), tint, handAlpha), lightCoords, overlayCoords);
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
     */
    private static void submitShells(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoating coating, HumanoidArm side,
                                     boolean slim, int lightCoords, float growth, boolean quiet) {
        QiCoatingDefinition.Shells shells = coating.definition().shells().orElse(null);
        if (shells == null || shells.layers().isEmpty()) {
            return;
        }
        if (shells.texture().isPresent()) {
            QiCoatingTextureShells.submitArm(poseStack, submitNodeCollector, shells.texture().get(), shells.layers(), side, slim,
                    lightCoords, growth);
            return;
        }
        QiCoatingLooks.ShellLook look = QiCoatingLooks.shell(shells.look(quiet).orElseThrow());
        if (look == null) {
            return;
        }
        // Matches the arm cube bounds in PlayerModel.createMesh.
        float width = slim ? 3.0F : 4.0F;
        float minX = side == HumanoidArm.RIGHT ? 1.0F - width : -1.0F;
        float maxX = minX + width;
        // Per-limb patterns start at their own point or style on each arm, so the two arms don't repeat each other.
        float shiftedStyle = look.perLimb()
                ? limbStyleOffset(look, side == HumanoidArm.RIGHT ? 0 : 1)
                : look.style() * 2.0F;
        List<QiCoatingDefinition.Layer> layers = shells.layers();
        submitNodeCollector.submitCustomGeometry(poseStack, renderType(look), (pose, buffer) -> {
            for (QiCoatingDefinition.Layer layer : layers) {
                emitShell(pose, buffer, minX, maxX, shiftedStyle, layer);
            }
        });
    }

    /**
     * The render type a shell look draws its boxes with (ShellLook.render).
     */
    private static RenderType renderType(QiCoatingLooks.ShellLook look) {
        return switch (look.render()) {
            case NORMAL -> ModRenderTypes.qiCoating();
            case DEPTH -> ModRenderTypes.qiCoatingDepth();
        };
    }

    /**
     * One shell layer round an arm: from `reach` pixels up from the hand tip down to `overhang` past it.
     */
    private static void emitShell(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float styleOffset,
                                  QiCoatingDefinition.Layer layer) {
        float topY = HAND_TIP_Y - layer.reach();
        float maxY = HAND_TIP_Y + layer.overhang();
        float minZ = ARM_MIN_Z - layer.inflate();
        float maxZ = ARM_MAX_Z + layer.inflate();
        // A square cap spans as much v across its depth as the sides do over the same distance, so square texels stay square on it.
        float capVSpan = layer.squareCap() ? (maxZ - minZ) / (maxY - topY) : CAP_V_SPAN;
        emitBox(pose, buffer, minX - layer.inflate(), maxX + layer.inflate(), topY, maxY, minZ, maxZ,
                styleOffset, layer.color(), false, capVSpan);
    }

    /**
     * Emits a box in part-local pixels, capped at maxY and optionally at minY; v runs from 0 at maxY (the hand end) to 1 at minY.
     *
     * @param capVSpan how much of the v range each cap spans across its depth
     */
    private static void emitBox(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float minY, float maxY,
                                float minZ, float maxZ, float styleOffset, int color, boolean topCap, float capVSpan) {
        float x0 = minX / 16.0F;
        float x1 = maxX / 16.0F;
        float z0 = minZ / 16.0F;
        float z1 = maxZ / 16.0F;
        float top = minY / 16.0F;
        float bottom = maxY / 16.0F;

        // u runs continuously around the perimeter so the shader's noise wraps without a seam. Each face gets an equal
        // quarter, so fract(u * 4) is the position across a face (exact for wide arms, slightly stretched on slim ones).
        float u0 = styleOffset;
        float u1 = styleOffset + 0.25F;
        float u2 = styleOffset + 0.5F;
        float u3 = styleOffset + 0.75F;
        float u4 = styleOffset + 1.0F;

        side(pose, buffer, x0, z0, x1, z0, u0, u1, top, bottom, color);
        side(pose, buffer, x1, z0, x1, z1, u1, u2, top, bottom, color);
        side(pose, buffer, x1, z1, x0, z1, u2, u3, top, bottom, color);
        side(pose, buffer, x0, z1, x0, z0, u3, u4, top, bottom, color);

        // Caps are mapped flat (u along x, v along z), carrying on from the first face's edge. Reusing the four corner u
        // values from the sides would make the two triangles of the quad interpolate differently and leave a V-shaped seam.
        // Cap over the maxY end (the fist, on arms).
        vertex(pose, buffer, x0, bottom, z0, u0, 0.0F, color);
        vertex(pose, buffer, x1, bottom, z0, u1, 0.0F, color);
        vertex(pose, buffer, x1, bottom, z1, u1, capVSpan, color);
        vertex(pose, buffer, x0, bottom, z1, u0, capVSpan, color);

        if (topCap) {
            vertex(pose, buffer, x0, top, z0, u0, 1.0F, color);
            vertex(pose, buffer, x1, top, z0, u1, 1.0F, color);
            vertex(pose, buffer, x1, top, z1, u1, 1.0F - capVSpan, color);
            vertex(pose, buffer, x0, top, z1, u0, 1.0F - capVSpan, color);
        }
    }

    private static void side(PoseStack.Pose pose, VertexConsumer buffer,
                             float ax, float az, float bx, float bz, float ua, float ub,
                             float top, float bottom, int color) {
        vertex(pose, buffer, ax, top, az, ua, 1.0F, color);
        vertex(pose, buffer, bx, top, bz, ub, 1.0F, color);
        vertex(pose, buffer, bx, bottom, bz, ub, 0.0F, color);
        vertex(pose, buffer, ax, bottom, az, ua, 0.0F, color);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
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

        private SkinCoatingConsumer(VertexConsumer delegate, Matrix4fc limbToWorld, boolean mantle, float progress, int impact,
                                    float centerX, float tipY) {
            super(delegate);
            this.limbToWorld = new Matrix4f(limbToWorld);
            this.worldToLimb = new Matrix4f(limbToWorld).invert();
            this.coating = (mantle ? 128 : 0) | (impact & 0x7F);
            this.impactAge = impact >> 8 & 0xFF;
            this.progress = Math.round(progress * 255.0F);
            this.centerX = centerX;
            this.tipY = tipY;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            // ModelPart vertices arrive already transformed; undo that to work in the limb's own pixels. Measured in 12px limb lengths
            // from the hand or foot (tipY) up, centred on centerX across and 0 front to back.
            Vector3f local = worldToLimb.transformPosition(x, y, z, scratch).mul(16.0F);
            float up = Mth.clamp((tipY - local.y) / 12.0F, 0.0F, 1.0F);
            local.add(Math.signum(local.x - centerX) * SKIN_COAT_LIFT, Math.signum(local.y - (tipY - 6.0F)) * SKIN_COAT_LIFT,
                    Math.signum(local.z) * SKIN_COAT_LIFT);
            Vector3f lifted = limbToWorld.transformPosition(local.div(16.0F));
            int data = ARGB.color(impactAge, coating, progress, Math.round(up * 255.0F));
            delegate.addVertex(lifted.x, lifted.y, lifted.z, data, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }

    }

    /**
     * Fades each vertex from opaque at the shoulder to handAlpha at the hand tip. Cube vertices only sit at the
     * two ends of the arm, so the GPU interpolates a smooth gradient between them.
     */
    private static final class ArmFadeConsumer extends DelegatingVertexConsumer {
        private final Matrix4f worldToArm;
        private final Vector3f scratch = new Vector3f();

        private final int tint;
        private final float handAlpha;

        private ArmFadeConsumer(VertexConsumer delegate, Matrix4fc armToWorld, int tint, float handAlpha) {
            super(delegate);
            this.worldToArm = new Matrix4f(armToWorld).invert();
            this.tint = tint;
            this.handAlpha = handAlpha;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            // ModelPart vertices arrive already transformed; undo that to get the height along the arm in pixels.
            float armY = worldToArm.transformPosition(x, y, z, scratch).y() * 16.0F;
            float progress = Mth.clamp((armY - SHOULDER_Y) / (HAND_TIP_Y - SHOULDER_Y), 0.0F, 1.0F);
            int faded = ARGB.multiplyAlpha(ARGB.multiply(color, tint), Mth.lerp(progress, 1.0F, handAlpha));
            delegate.addVertex(x, y, z, faded, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }

    }
}
