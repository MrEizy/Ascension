package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class SwordFlightPhysics {
    private SwordFlightPhysics() {
    }

    // ── Tuning ──────────────────────────────────────────────
    private static final double MAX_SPEED = 1.6;
    private static final double BOOST_MAX_SPEED = 2.4;
    private static final double ACCELERATION = 0.09;
    private static final double TURN_ACCELERATION = 0.16;
    private static final double DRAG = 0.985;
    private static final double DIVE_SPEED_BONUS = 0.35;
    private static final double CLIMB_SPEED_PENALTY = 0.25;
    private static final double MAX_BANK_DEGREES = 35.0;
    private static final double BANK_LERP = 0.15;
    private static final double SPEED_RAMP = 0.02;
    private static final double MIN_SPEED_SCALE = 0.35;
    private static final double TURN_SPEED_LOSS = 0.05; // max fraction bled per tick at a full 90+ reversal
    private static final double SPEED_SCALE_LERP = 0.06;
    private static final double TURN_SIGN_DEADZONE_DEGREES = 1.5; // avoids bank sign flicker near dead-straight
    private static final float BODY_ROTATION_LERP = 0.35F; // how hard this fights vanilla's own body-turn each tick
    private static final float SKATEBOARD_OFFSET_DEGREES = 90.0F;
    private static final double MIN_SKATE_SPEED = 0.05; // below this, leave body rotation alone (avoids atan2 noise)

    // Per-entity transient flight state. Not networked directly — vanilla entity
    // motion sync already carries velocity, and bank/speed-scale are client-cosmetic.
    private static final Map<UUID, FlightState> ACTIVE = new ConcurrentHashMap<>();

    /** Called by SkillActions.SwordFlight.apply() — toggles flight for the caster. */
    public static void toggle(LivingEntity caster) {
        if (caster == null) {
            return;
        }
        if (ACTIVE.containsKey(caster.getUUID())) {
            endFlight(caster);
        } else if (heldSword(caster) != null) {
            beginFlight(caster);
        }
    }

    /** Read-only check for the renderer — is this entity currently sword-flying? */
    public static boolean isFlying(UUID entityId) {
        return ACTIVE.containsKey(entityId);
    }

    /** Interpolated bank angle for this frame's partial tick — shared by camera roll and the renderer's tilt. */
    public static double getBankDegrees(UUID entityId, float partialTick) {
        FlightState state = ACTIVE.get(entityId);
        if (state == null) {
            return 0.0;
        }
        return Mth.lerp(partialTick, state.bankDegreesO, state.bankDegrees);
    }

    /**
     * Pitch angle derived from actual flight velocity (climb/dive), not look
     * direction — positive means diving (nose down), negative means climbing
     * (nose up), matching the same sign convention as LivingEntity#getXRot().
     * Physics-driven rather than camera-driven, so the model tilts with how
     * the caster is actually moving, not just where they're looking.
     */
    public static double getVelocityPitchDegrees(UUID entityId) {
        FlightState state = ACTIVE.get(entityId);
        if (state == null) {
            return 0.0;
        }
        double horizontal = state.velocity.horizontalDistance();
        if (horizontal < 1.0E-4 && Math.abs(state.velocity.y) < 1.0E-4) {
            return 0.0;
        }
        return -Math.toDegrees(Math.atan2(state.velocity.y, horizontal));
    }

    /** Public so the renderer can grab the same sword without duplicating the tag check. */
    public static ItemStack heldSword(LivingEntity entity) {
        ItemStack main = entity.getMainHandItem();
        if (main.is(ItemTags.SWORDS)) {
            return main;
        }
        ItemStack off = entity.getOffhandItem();
        return off.is(ItemTags.SWORDS) ? off : null;
    }

    // ── Activation ──────────────────────────────────────────────────────────

    private static void beginFlight(LivingEntity entity) {
        entity.setNoGravity(true);
        entity.fallDistance = 0.0F;
        FlightState state = ACTIVE.computeIfAbsent(entity.getUUID(), id -> new FlightState());
        state.velocity = entity.getDeltaMovement();
    }

    private static void endFlight(LivingEntity entity) {
        entity.setNoGravity(false);
        ACTIVE.remove(entity.getUUID());
    }

    // ── Physics — velocity-based, fully continuous (no branch = no click) ──

    private static void tickFlight(LivingEntity entity, boolean boosting) {
        FlightState state = ACTIVE.get(entity.getUUID());
        if (state == null) {
            return;
        }

        Vec3 look = entity.getViewVector(1.0F);
        Vec3 desired = look;

        // xxa is the entity's live sideways input (public on LivingEntity, decays
        // via applyInput() each tick) — works for any LivingEntity, no Player cast.
        double strafe = entity.xxa;
        if (Math.abs(strafe) > 1.0E-3) {
            Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
            desired = look.add(right.scale(strafe * 0.6)).normalize();
        }

        double maxSpeed = boosting ? BOOST_MAX_SPEED : MAX_SPEED;
        double pitchFactor = Mth.clamp(entity.getXRot() / 90.0, -1.0, 1.0); // >0 looking down (dive)
        maxSpeed += pitchFactor > 0 ? DIVE_SPEED_BONUS * pitchFactor : CLIMB_SPEED_PENALTY * pitchFactor;
        maxSpeed = Math.max(0.2, maxSpeed);

        Vec3 current = state.velocity;
        Vec3 desiredVelocity = desired.scale(maxSpeed * state.speedScale);

        // turnFactor: 0 = flying straight, 1 = a full reversal. Everything below is
        // a continuous function of it — nothing changes state at a fixed threshold.
        double turnAngle = angleBetweenDegrees(current, desiredVelocity);
        double turnFactor = Mth.clamp(turnAngle / 90.0, 0.0, 1.0);

        double accel = Mth.lerp(turnFactor, ACCELERATION, TURN_ACCELERATION);
        current = current.scale(1.0 - turnFactor * TURN_SPEED_LOSS);

        double speedScaleTarget = Mth.lerp(turnFactor, Math.min(1.0, state.speedScale + SPEED_RAMP), MIN_SPEED_SCALE);
        state.speedScale = Mth.lerp(SPEED_SCALE_LERP, state.speedScale, speedScaleTarget);
        desiredVelocity = desired.scale(maxSpeed * state.speedScale);

        Vec3 newVelocity = current.add(desiredVelocity.subtract(current).scale(accel)).scale(DRAG);
        if (newVelocity.length() > maxSpeed) {
            newVelocity = newVelocity.normalize().scale(maxSpeed);
        }

        double turnSign = turnAngle > TURN_SIGN_DEADZONE_DEGREES
                ? Math.signum(current.x * desired.z - current.z * desired.x)
                : 0.0;
        double desiredBank = Mth.clamp(turnSign * turnAngle, -MAX_BANK_DEGREES, MAX_BANK_DEGREES);
        state.bankDegreesO = state.bankDegrees;
        state.bankDegrees = Mth.lerp(BANK_LERP, state.bankDegrees, desiredBank);

        state.velocity = newVelocity;
        entity.setDeltaMovement(newVelocity);
        entity.fallDistance = 0.0F;

        applySkateboardStance(entity, newVelocity);
    }

    /**
     * Rotates the caster's body — not the head/camera — perpendicular to travel
     * direction, the "standing sideways on a skateboard" look. yBodyRot is public
     * and separate from getYRot()/yHeadRot (confirmed straight off LivingEntity),
     * so this never touches where the player is actually looking.
     */
    private static void applySkateboardStance(LivingEntity entity, Vec3 velocity) {
        if (velocity.horizontalDistanceSqr() < MIN_SKATE_SPEED * MIN_SKATE_SPEED) {
            return;
        }
        float heading = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0 / Math.PI)) - 90.0F;
        float target = heading + SKATEBOARD_OFFSET_DEGREES;
        float delta = Mth.wrapDegrees(target - entity.yBodyRot);
        entity.yBodyRot += delta * BODY_ROTATION_LERP;
    }

    private static double angleBetweenDegrees(Vec3 a, Vec3 b) {
        if (a.lengthSqr() < 1.0E-6 || b.lengthSqr() < 1.0E-6) {
            return 0.0;
        }
        double cos = Mth.clamp(a.normalize().dot(b.normalize()), -1.0, 1.0);
        return Math.toDegrees(Math.acos(cos));
    }

    private static final class FlightState {
        Vec3 velocity = Vec3.ZERO;
        double speedScale = MIN_SPEED_SCALE;
        double bankDegrees;
        double bankDegreesO;
    }

    // ── Server-authoritative tick; also auto-lands if the sword leaves the hand ──
    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerTick {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide() || !ACTIVE.containsKey(entity.getUUID())) {
                return;
            }
            if (heldSword(entity) == null) {
                endFlight(entity);
                return;
            }
            tickFlight(entity, entity.isSprinting());
        }
    }

    // ── Client-side prediction (local player only) + camera roll ──────────
    @EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
    public static final class ClientTick {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player local = Minecraft.getInstance().player;
            if (local == null || event.getEntity() != local || !ACTIVE.containsKey(local.getUUID())) {
                return;
            }
            tickFlight(local, local.isSprinting());
        }

        @SubscribeEvent
        public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            Player local = Minecraft.getInstance().player;
            if (local == null || !isFlying(local.getUUID())) {
                return;
            }

            event.setRoll((float) getBankDegrees(local.getUUID(), (float) event.getPartialTick()));
        }
    }
}