package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.RealmEffectivenessConfiguration;
import net.zic.ascension.network.SwordFlightStatePacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SwordFlightPhysics {
    private SwordFlightPhysics() {
    }

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
    private static final double MIN_SKATE_SPEED = 0.05;
    private static final double SPEED_REALM_EXPONENT = 0.025D;
    private static final double ACCELERATION_REALM_EXPONENT = 0.0125D;

    private static final Map<UUID, FlightState> SERVER_ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, FlightState> CLIENT_ACTIVE = new ConcurrentHashMap<>();

    public static void toggle(LivingEntity caster) {
        if (caster == null || caster.level().isClientSide()) {
            return;
        }
        if (SERVER_ACTIVE.containsKey(caster.getUUID())) {
            endFlight(caster);
        } else if (heldSword(caster) != null) {
            beginFlight(caster);
        }
    }

    public static boolean isFlying(UUID entityId) {
        return CLIENT_ACTIVE.containsKey(entityId);
    }

    public static double getBankDegrees(UUID entityId, float partialTick) {
        FlightState state = CLIENT_ACTIVE.get(entityId);
        if (state == null) {
            return 0.0;
        }
        return Mth.lerp(partialTick, state.bankDegreesO, state.bankDegrees);
    }

    public static double getVelocityPitchDegrees(UUID entityId) {
        FlightState state = CLIENT_ACTIVE.get(entityId);
        if (state == null) {
            return 0.0;
        }
        double horizontal = state.velocity.horizontalDistance();
        if (horizontal < 1.0E-4 && Math.abs(state.velocity.y) < 1.0E-4) {
            return 0.0;
        }
        return -Math.toDegrees(Math.atan2(state.velocity.y, horizontal));
    }

    public static ItemStack heldSword(LivingEntity entity) {
        InteractionHand hand = heldSwordHand(entity);
        return hand == null ? null : entity.getItemInHand(hand);
    }

    public static InteractionHand heldSwordHand(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getMainHandItem().is(ItemTags.SWORDS)) {
            return InteractionHand.MAIN_HAND;
        }
        return entity.getOffhandItem().is(ItemTags.SWORDS) ? InteractionHand.OFF_HAND : null;
    }

    public static void setClientFlightState(UUID entityId, boolean active) {
        if (entityId == null) {
            return;
        }
        if (!active) {
            CLIENT_ACTIVE.remove(entityId);
            return;
        }
        FlightState state = CLIENT_ACTIVE.computeIfAbsent(entityId, id -> new FlightState());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            var entity = minecraft.level.getPlayerByUUID(entityId);
            if (entity != null) {
                state.velocity = entity.getDeltaMovement();
            }
        }
    }

    public static void clearClientFlightStates() {
        CLIENT_ACTIVE.clear();
    }

    private static void beginFlight(LivingEntity entity) {
        FlightState state = SERVER_ACTIVE.computeIfAbsent(entity.getUUID(), id -> new FlightState());
        state.hadNoGravity = entity.isNoGravity();
        state.velocity = entity.getDeltaMovement();
        entity.setNoGravity(true);
        entity.fallDistance = 0.0F;
        syncFlightState(entity.getUUID(), true, entity.level().getServer());
    }

    private static void endFlight(LivingEntity entity) {
        FlightState state = SERVER_ACTIVE.remove(entity.getUUID());
        if (state == null) {
            return;
        }
        entity.setNoGravity(state.hadNoGravity);
        syncFlightState(entity.getUUID(), false, entity.level().getServer());
    }

    private static void tickFlight(LivingEntity entity, boolean boosting, FlightState state) {
        Vec3 look = entity.getViewVector(1.0F);
        Vec3 desired = look;

        double strafe = entity.xxa;
        if (Math.abs(strafe) > 1.0E-3) {
            Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
            desired = look.add(right.scale(strafe * 0.6)).normalize();
        }

        double speedMultiplier = RealmEffectivenessConfiguration.getGameplayMultiplier(entity, SPEED_REALM_EXPONENT);
        double accelerationMultiplier = RealmEffectivenessConfiguration.getGameplayMultiplier(entity, ACCELERATION_REALM_EXPONENT);
        double maxSpeed = boosting ? BOOST_MAX_SPEED : MAX_SPEED;
        double pitchFactor = Mth.clamp(entity.getXRot() / 90.0, -1.0, 1.0);
        maxSpeed += pitchFactor > 0 ? DIVE_SPEED_BONUS * pitchFactor : CLIMB_SPEED_PENALTY * pitchFactor;
        maxSpeed = Math.max(0.2, maxSpeed) * speedMultiplier;

        Vec3 current = entity.getDeltaMovement();
        Vec3 desiredVelocity = desired.scale(maxSpeed * state.speedScale);
        double turnAngle = angleBetweenDegrees(current, desiredVelocity);
        double turnFactor = Mth.clamp(turnAngle / 90.0, 0.0, 1.0);

        double accel = Mth.lerp(turnFactor, ACCELERATION, TURN_ACCELERATION) * accelerationMultiplier;
        accel = Mth.clamp(accel, 0.0, 0.45);
        current = current.scale(1.0 - turnFactor * TURN_SPEED_LOSS);

        double speedRamp = SPEED_RAMP * accelerationMultiplier;
        double speedScaleTarget = Mth.lerp(turnFactor, Math.min(1.0, state.speedScale + speedRamp), MIN_SPEED_SCALE);
        state.speedScale = Mth.lerp(SPEED_SCALE_LERP, state.speedScale, speedScaleTarget);
        desiredVelocity = desired.scale(maxSpeed * state.speedScale);

        Vec3 newVelocity = current.add(desiredVelocity.subtract(current).scale(accel)).scale(DRAG);
        if (newVelocity.length() > maxSpeed) {
            newVelocity = newVelocity.normalize().scale(maxSpeed);
        }

        updateBank(state, current, desiredVelocity);
        state.velocity = newVelocity;
        entity.setDeltaMovement(newVelocity);
        entity.fallDistance = 0.0F;
        applySkateboardStance(entity, newVelocity);
    }

    private static void tickRemoteVisuals(Player entity, FlightState state) {
        Vec3 observed = entity.getDeltaMovement();
        state.bankDegreesO = state.bankDegrees;
        if (state.velocity.lengthSqr() > 1.0E-6 && observed.lengthSqr() > 1.0E-6) {
            double turnAngle = angleBetweenDegrees(state.velocity, observed);
            double turnSign = turnAngle > TURN_SIGN_DEADZONE_DEGREES ? Math.signum(state.velocity.x * observed.z - state.velocity.z * observed.x) : 0.0;
            double desiredBank = Mth.clamp(turnSign * turnAngle, -MAX_BANK_DEGREES, MAX_BANK_DEGREES);
            state.bankDegrees = Mth.lerp(BANK_LERP, state.bankDegrees, desiredBank);
        } else {
            state.bankDegrees = Mth.lerp(BANK_LERP, state.bankDegrees, 0.0);
        }
        state.velocity = observed;
        applySkateboardStance(entity, observed);
    }

    private static void updateBank(FlightState state, Vec3 current, Vec3 desiredVelocity) {
        double turnAngle = angleBetweenDegrees(current, desiredVelocity);
        double turnSign = turnAngle > TURN_SIGN_DEADZONE_DEGREES
                ? Math.signum(current.x * desiredVelocity.z - current.z * desiredVelocity.x)
                : 0.0;
        double desiredBank = Mth.clamp(turnSign * turnAngle, -MAX_BANK_DEGREES, MAX_BANK_DEGREES);
        state.bankDegreesO = state.bankDegrees;
        state.bankDegrees = Mth.lerp(BANK_LERP, state.bankDegrees, desiredBank);
    }

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

    private static void syncFlightState(UUID entityId, boolean active, MinecraftServer server) {
        if (server == null) {
            return;
        }
        SwordFlightStatePacket packet = new SwordFlightStatePacket(entityId, active);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    private static void syncActiveFlights(ServerPlayer viewer) {
        for (UUID entityId : SERVER_ACTIVE.keySet()) {
            PacketDistributor.sendToPlayer(viewer, new SwordFlightStatePacket(entityId, true));
        }
    }

    private static final class FlightState {
        Vec3 velocity = Vec3.ZERO;
        double speedScale = MIN_SPEED_SCALE;
        double bankDegrees;
        double bankDegreesO;
        boolean hadNoGravity;
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerEvents {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player entity = event.getEntity();
            if (entity.level().isClientSide()) {
                return;
            }
            FlightState state = SERVER_ACTIVE.get(entity.getUUID());
            if (state == null) {
                return;
            }
            if (heldSword(entity) == null) {
                endFlight(entity);
                return;
            }
            tickFlight(entity, entity.isSprinting(), state);
        }

        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                syncActiveFlights(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                endFlight(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                endFlight(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                endFlight(player);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
                endFlight(player);
            }
        }
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                return;
            }
            FlightState state = CLIENT_ACTIVE.get(player.getUUID());
            if (state == null) {
                return;
            }
            Player local = Minecraft.getInstance().player;
            if (player == local) {
                tickFlight(player, player.isSprinting(), state);
            } else {
                tickRemoteVisuals(player, state);
            }
        }

        @SubscribeEvent
        public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            clearClientFlightStates();
        }

        @SubscribeEvent
        public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            Player local = Minecraft.getInstance().player;
            if (local == null || !isFlying(local.getUUID())) {
                return;
            }
            event.setRoll(event.getRoll() + (float) getBankDegrees(local.getUUID(), (float) event.getPartialTick()));
        }
    }
}
