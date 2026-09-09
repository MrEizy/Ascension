package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
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

/**
 * TODO Make it less janky and properly introduce the model of the sword as it lays down under you.
 */
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
        // else: no sword and not currently flying — no-op.
    }

    private static ItemStack heldSword(LivingEntity entity) {
        ItemStack main = entity.getMainHandItem();
        if (main.is(ItemTags.SWORDS)) {
            return main;
        }
        ItemStack off = entity.getOffhandItem();
        return off.is(ItemTags.SWORDS) ? off : null;
    }

    // ── Activation / visual ─────────────────────────────────────────────────

    private static void beginFlight(LivingEntity entity) {
        entity.setNoGravity(true);
        entity.fallDistance = 0.0F;
        FlightState state = ACTIVE.computeIfAbsent(entity.getUUID(), id -> new FlightState());
        state.velocity = entity.getDeltaMovement();
        if (!entity.level().isClientSide() && state.displayId == null) {
            state.displayId = spawnSwordDisplay(entity);
        }
    }

    private static void endFlight(LivingEntity entity) {
        entity.setNoGravity(false);
        FlightState state = ACTIVE.remove(entity.getUUID());
        if (state != null && state.displayId != null && entity.level() instanceof ServerLevel serverLevel) {
            Entity display = serverLevel.getEntity(state.displayId);
            if (display != null) {
                display.discard();
            }
        }
    }

    /** Spawns the floating sword the caster stands on top of. */
    private static UUID spawnSwordDisplay(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        ItemStack sword = heldSword(entity);
        if (sword == null) {
            return null;
        }
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);
        SlotAccess itemSlot = display.getSlot(0);
        if (itemSlot != null) {
            itemSlot.set(sword.copy());
        }
        display.setPos(entity.getX(), entity.getY() - 0.1D, entity.getZ());
        display.setYRot(entity.getYRot());
        serverLevel.addFreshEntity(display);
        return display.getUUID();
    }

    private static void updateSwordDisplay(LivingEntity entity, FlightState state) {
        if (state.displayId == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity display = serverLevel.getEntity(state.displayId);
        if (display == null) {
            state.displayId = null;
            return;
        }
        display.setPos(entity.getX(), entity.getY() - 0.1D, entity.getZ());
        display.setYRot(entity.getYRot());
    }

    // ── Physics — velocity-based, not position-based ───────────────────────

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

        state.speedScale = Mth.clamp(state.speedScale + SPEED_RAMP, MIN_SPEED_SCALE, 1.0);

        Vec3 current = state.velocity;
        Vec3 desiredVelocity = desired.scale(maxSpeed * state.speedScale);

        double turnAngle = angleBetweenDegrees(current, desiredVelocity);
        if (turnAngle > 45.0) {
            current = current.scale(0.97); // carving a hard turn bleeds speed
            state.speedScale = Mth.clamp(state.speedScale - SPEED_RAMP * 2.0, MIN_SPEED_SCALE, 1.0);
        }

        double accel = turnAngle > 30.0 ? TURN_ACCELERATION : ACCELERATION;
        Vec3 newVelocity = current.add(desiredVelocity.subtract(current).scale(accel)).scale(DRAG);
        if (newVelocity.length() > maxSpeed) {
            newVelocity = newVelocity.normalize().scale(maxSpeed);
        }

        double turnSign = Math.signum(current.x * desired.z - current.z * desired.x);
        double desiredBank = Mth.clamp(turnSign * turnAngle, -MAX_BANK_DEGREES, MAX_BANK_DEGREES);
        state.bankDegrees = Mth.lerp(BANK_LERP, state.bankDegrees, desiredBank);

        state.velocity = newVelocity;
        entity.setDeltaMovement(newVelocity);
        entity.fallDistance = 0.0F;

        updateSwordDisplay(entity, state);
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
        UUID displayId;
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
            if (local == null) {
                return;
            }
            FlightState state = ACTIVE.get(local.getUUID());
            if (state == null) {
                return;
            }
            event.setRoll((float) state.bankDegrees);
        }
    }
}