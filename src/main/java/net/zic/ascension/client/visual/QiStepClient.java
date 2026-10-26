package net.zic.ascension.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.ModRenderTypes;
import net.zic.ascension.impl.core.skill.passive.PassiveModifiers;
import net.zic.ascension.impl.core.skill.passive.QiStepService;
import net.zic.ascension.network.QiStepActionPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side of Qi Steps (PassiveModifiers.QiStep). For your own player: once you are falling through the air with Sky
 * Stride on, footholds of qi form under your feet and you walk, run and jump on them as if on ground: each tick you are held
 * at your height and counted as standing, so vanilla ground movement, sprinting and jumping all apply. A foothold forms under
 * the stepping foot every stepDistance blocks, alternating feet; a jump, and each charge of standing still (up to hoverTicks
 * per trip), puts one under both feet. Each is reported to the server, which pays for it and may end the trip (setState).
 * Sneaking drops you. Every player's footholds are drawn in the world (shader style 56), so you see your own in first person.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class QiStepClient {
    // Below this horizontal speed with no movement keys held, you count as standing still.
    private static final double STILL_SPEED = 0.03D;
    // An upward speed above this, right after you were held up, means vanilla just jumped you off the footholds.
    private static final double JUMP_DETECT_SPEED = 0.2D;
    // Footholds only catch you with at least this many blocks of air below, more than a normal jump's 1.25.
    private static final double MIN_DROP = 1.5D;
    // Upward speed of the mid-air double jump that starts a trip, a normal jump's.
    private static final double DOUBLE_JUMP_SPEED = 0.42D;
    // Footholds: FOOTHOLD_SIZE blocks across (a foot is 4px, 0.23 blocks), FOOTHOLD_LIFT below the feet, FOOT_SIDE blocks either
    // side of the player's centre, lasting FOOTHOLD_TICKS and fading over the last FOOTHOLD_FADE_TICKS.
    private static final double FOOTHOLD_SIZE = 0.28D;
    private static final double FOOTHOLD_LIFT = 0.01D;
    private static final double FOOT_SIDE = 0.12D;
    private static final int FOOTHOLD_TICKS = 30;
    private static final int FOOTHOLD_FADE_TICKS = 15;
    private static final float FOOTHOLD_STYLE_OFFSET = 56.0F * 2.0F;

    private static PassiveModifiers.QiStep settings;
    // This trip through the air: spent (out of qi or strain), strain so far, whether footholds are holding you, whether they
    // held you this tick, distance walked since the last foothold, which foot steps next, ticks spent standing still, and ticks
    // toward the next standing charge.
    private static boolean exhausted;
    private static double strain;
    private static boolean supported;
    private static boolean heldThisTick;
    private static double stepTravel;
    private static int nextFoot;
    private static int hoverTicks;
    private static int hoverCharge;
    // Whether this trip has started (so the minimum drop no longer applies), and the jump key's state last tick.
    private static boolean tripStarted;
    private static boolean wasJumpDown;
    private static final List<Foothold> FOOTHOLDS = new ArrayList<>();

    private QiStepClient() {
    }

    /**
     * The trip so far (strain, time spent standing still) is kept through Qi Steps being switched off and on; only landing
     * resets it, as on the server.
     */
    public static void setState(PassiveModifiers.QiStep newSettings, boolean newExhausted) {
        settings = newSettings;
        exhausted = newExhausted;
        if (newSettings == null || newExhausted) {
            supported = false;
        }
    }

    /**
     * A foothold at `position` facing `yaw` (body yaw, degrees), or with `bothFeet` one under each foot either side of it.
     */
    public static void addFootholds(Vec3 position, float yaw, boolean bothFeet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long now = minecraft.level.getGameTime();
        if (!bothFeet) {
            FOOTHOLDS.add(new Foothold(position, yaw, now));
            return;
        }
        Vec3 side = footSide(yaw);
        FOOTHOLDS.add(new Foothold(position.add(side), yaw, now));
        FOOTHOLDS.add(new Foothold(position.subtract(side), yaw, now));
    }

    /**
     * From the player's centre to their right foot.
     */
    private static Vec3 footSide(float yaw) {
        float radians = yaw * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(radians), 0.0D, -Mth.sin(radians)).scale(FOOT_SIDE);
    }

    private static void resetTrip() {
        strain = 0.0D;
        supported = false;
        heldThisTick = false;
        stepTravel = 0.0D;
        hoverTicks = 0;
        hoverCharge = 0;
        tripStarted = false;
    }

    /**
     * Before your own player moves each tick: holds them at their height and counts them as standing, so this tick's movement
     * and any jump are vanilla ground ones.
     */
    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        heldThisTick = false;
        if (minecraft.level == null) {
            FOOTHOLDS.clear();
        }
        if (player == null || minecraft.isPaused()) {
            return;
        }
        boolean jumpDown = minecraft.options.keyJump.isDown();
        boolean jumpPressed = jumpDown && !wasJumpDown;
        wasJumpDown = jumpDown;
        // Checked even while Qi Steps is off, so landing with it off still ends the trip.
        if (player.onGround() || player.isInWater() || player.isInLava() || player.getAbilities().flying || player.isFallFlying()
                || player.isPassenger() || player.onClimbable() || player.isSpectator()) {
            resetTrip();
            exhausted = false;
            return;
        }
        if (settings == null) {
            return;
        }
        Vec3 velocity = player.getDeltaMovement();
        // Strain cap off for now: Qi Steps lasts as long as your qi does.
        if (exhausted /* || strain >= settings.maxStrain() */ || player.isShiftKeyDown()) {
            supported = false;
            return;
        }
        if (!supported) {
            if (jumpPressed && !tripStarted) {
                // Double jump: pressing jump again in mid-air kicks off a foothold under both feet and starts the trip.
                tripStarted = true;
                strain += settings.jumpStrain();
                player.setDeltaMovement(velocity.x, DOUBLE_JUMP_SPEED, velocity.z);
                formFootholds(player, QiStepService.Action.JUMP, player.position().subtract(0.0D, FOOTHOLD_LIFT, 0.0D), true);
                return;
            }
            // Only catch you once you start to fall, so jumps rise as normal.
            if (velocity.y > 0.0D) {
                return;
            }
            if (!tripStarted && !player.level().noCollision(player, player.getBoundingBox().expandTowards(0.0D, -MIN_DROP, 0.0D))) {
                return;
            }
            supported = true;
            tripStarted = true;
            stepTravel = settings.stepDistance();
            hoverCharge = QiStepService.HOVER_CHARGE_TICKS;
        }
        boolean moving = player.xxa != 0.0F || player.zza != 0.0F || velocity.horizontalDistance() > STILL_SPEED;
        // Standing still limit off for now: standing lasts as long as your qi does.
        // if (!moving && ++hoverTicks > settings.hoverTicks()) {
        //     // Standing still on qi can't last: the footholds give way.
        //     exhausted = true;
        //     supported = false;
        //     return;
        // }
        player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
        player.setOnGround(true);
        player.resetFallDistance();
        heldThisTick = true;
        Vec3 centre = player.position().subtract(0.0D, FOOTHOLD_LIFT, 0.0D);
        if (moving) {
            stepTravel += velocity.horizontalDistance();
            if (stepTravel >= settings.stepDistance()) {
                stepTravel = 0.0D;
                strain += 1.0D;
                Vec3 side = footSide(player.yBodyRot);
                Vec3 foot = nextFoot == 0 ? centre.add(side) : centre.subtract(side);
                nextFoot = 1 - nextFoot;
                formFootholds(player, QiStepService.Action.STEP, foot, false);
            }
        } else if (++hoverCharge >= QiStepService.HOVER_CHARGE_TICKS) {
            hoverCharge = 0;
            strain += settings.hoverStrain();
            formFootholds(player, QiStepService.Action.HOVER, centre, true);
        }
    }

    /**
     * After your own player moved: if vanilla jumped them off the footholds, the jump is charged and leaves a foothold under
     * both feet, and they are caught again once they start to fall.
     */
    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!heldThisTick || player == null || settings == null || player.getDeltaMovement().y < JUMP_DETECT_SPEED) {
            return;
        }
        heldThisTick = false;
        supported = false;
        strain += settings.jumpStrain();
        Vec3 takeOff = player.position().subtract(0.0D, player.getDeltaMovement().y + FOOTHOLD_LIFT, 0.0D);
        formFootholds(player, QiStepService.Action.JUMP, takeOff, true);
    }

    private static void formFootholds(LocalPlayer player, QiStepService.Action action, Vec3 position, boolean bothFeet) {
        addFootholds(position, player.yBodyRot, bothFeet);
        ClientPacketDistributor.sendToServer(new QiStepActionPacket(action, position));
    }

    /**
     * Every recent foothold as a small flat square, lined up with the body of the player who made it, its vertex alpha fading
     * at the end of its life (the shader also breaks it up as it fades).
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || FOOTHOLDS.isEmpty()) {
            return;
        }
        float time = minecraft.level.getGameTime() + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        FOOTHOLDS.removeIf(foothold -> time - foothold.spawnTick() > FOOTHOLD_TICKS);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(ModRenderTypes.qiCoating());
        for (Foothold foothold : FOOTHOLDS) {
            float life = Math.min(1.0F, (foothold.spawnTick() + FOOTHOLD_TICKS - time) / FOOTHOLD_FADE_TICKS);
            if (life <= 0.0F) {
                continue;
            }
            int color = ARGB.white(life);
            float yaw = foothold.yaw() * Mth.DEG_TO_RAD;
            Vec3 x = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw)).scale(FOOTHOLD_SIZE * 0.5D);
            Vec3 z = new Vec3(-x.z, 0.0D, x.x);
            Vec3 centre = foothold.position().subtract(camera);
            footholdVertex(pose, buffer, centre.subtract(x).subtract(z), 0.0F, 0.0F, color);
            footholdVertex(pose, buffer, centre.add(x).subtract(z), 1.0F, 0.0F, color);
            footholdVertex(pose, buffer, centre.add(x).add(z), 1.0F, 1.0F, color);
            footholdVertex(pose, buffer, centre.subtract(x).add(z), 0.0F, 1.0F, color);
        }
        buffers.endLastBatch();
    }

    private static void footholdVertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, float u, float v, int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setUv(FOOTHOLD_STYLE_OFFSET + u, v)
                .setColor(color);
    }

    private record Foothold(Vec3 position, float yaw, long spawnTick) {
    }
}
