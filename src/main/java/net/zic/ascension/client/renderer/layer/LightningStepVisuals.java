package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import net.zic.ascension.client.renderer.ModRenderTypes;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightning Step's visuals (QiCoatingEffects.LIGHTNING_STEP): the crackling footprints left on the
 * ground, and the spear flash (started by the server, QiCoatingService) that drives the player forward and leaves a lightning
 * trail. It is the effect's QiCoatingEffect: QiCoatingLayer ticks it per player, draws its ground effects with the player, and
 * draws the player see-through at STEP_BODY_ALPHA.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class LightningStepVisuals implements QiCoatingEffect {
    static final LightningStepVisuals INSTANCE = new LightningStepVisuals();

    // Ground effects (footprints and the spear flash trail) are drawn as crackling bolts (shader style 51 for
    // the trail) lifted TRAIL_LIFT off the ground; feet sit TRAIL_FOOT_SIDE blocks either side of the player's centre.
    private static final float TRAIL_STYLE_OFFSET = 51.0F * 2.0F;
    private static final double TRAIL_LIFT = 0.02D;
    private static final double TRAIL_FOOT_SIDE = 0.12D;
    // Footprints: a crackling mark (shader styles 52-55) put down every MARK_SPACING blocks walked on the
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
    // Spear flash (started by the server, QiCoatingService): the player is driven forward at FLASH_SPEED blocks a
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

    // The player's own model is drawn at this opacity while active.
    private static final float STEP_BODY_ALPHA = 0.9F;
    // PlayerRenderer scales the model by this before drawing it.
    private static final float PLAYER_MODEL_SCALE = 0.9375F;

    /**
     * Spear flash, from LightningFlashPacket: drives your own player forward if it is you, and starts recording
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
     * Drops expired footprints and puts down new ones as the player walks or lands, for submitFootMarks.
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
     * Each footprint as a flat square on the ground, turned to its own angle, its vertex alpha fading over
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
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
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
     * Spear flash: a jagged bolt along the path the player just flashed down, as a flat and an upright ribbon
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
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
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
     * Spear flash: the path one player flashed down, recorded tick by tick.
     */
    private static final class FlashTrail {
        private final List<Vec3> points = new ArrayList<>();
        private int recordTicksLeft;
        private long endTick;
    }

    /**
     * One player's footprints and what decides the next one.
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
        buffer.addVertex(pose, p.x, p.y, p.z).setUv(u, v).setColor(color);
    }

    private LightningStepVisuals() {
    }

    /**
     * Their footprints while their coating has Lightning Step, forgotten when it doesn't.
     */
    @Override
    public void tickPlayer(ClientLevel level, AbstractClientPlayer player, boolean active) {
        if (active) {
            recordFootMarks(level, player);
        } else {
            FOOT_MARKS.remove(player.getId());
        }
    }

    /**
     * Forgets players who left the level, and records the flash trails.
     */
    @Override
    public void tickLevel(ClientLevel level) {
        FOOT_MARKS.keySet().removeIf(id -> level.getEntity(id) == null);
        recordFlashTrails(level);
    }

    /**
     * The player's ground effects: their footprints and flash trails.
     */
    @Override
    public void submitBody(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state) {
        submitFootMarks(poseStack, submitNodeCollector, state);
        submitFlashTrail(poseStack, submitNodeCollector, state);
    }

    @Override
    public float bodyAlpha() {
        return STEP_BODY_ALPHA;
    }

    /**
     * In first person the body isn't drawn, so the arm itself goes see-through here (vanilla's is left out, bodyAlpha).
     */
    @Override
    public void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Arm arm) {
        QiCoatingLayer.submitGhostArm(poseStack, submitNodeCollector, arm.skin(), arm.lightCoords(), arm.overlayCoords(), arm.side(),
                arm.slim(), arm.sleeve(), ARGB.white(STEP_BODY_ALPHA));
    }
}
