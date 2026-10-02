package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.configuration.RealmEffectivenessConfiguration;
import net.zic.ascension.network.SphericalDestructionPayload;
import net.zic.ascension.network.SphericalProjectilePayload;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SphericalDestructionService {
    private SphericalDestructionService() {
    }

    private static final Map<UUID, ProjectileState> ACTIVE = new ConcurrentHashMap<>();
    private static final double DAMAGE_REALM_EXPONENT = 0.12D;
    private static final double RADIUS_REALM_EXPONENT = 0.04D;
    private static final double RANGE_REALM_EXPONENT = 0.06D;
    private static final double MAX_RADIUS_MULTIPLIER = 3.0D;
    private static final double MAX_RANGE_MULTIPLIER = 6.0D;
    private static final int MAX_EFFECT_RADIUS = 64;
    private static final int DETONATION_DELAY_TICKS = 12;
    private static final int IMPACT_VISUAL_TICKS = 30;
    private static final double VISUAL_TRACKING_RANGE = 384.0D;
    private static final int EFFECT_COLOR = 0xFF6A1B;

    public static void launch(SkillActionContext context, SkillActions.SphericalDestruction config) {
        if (!(context.level() instanceof ServerLevel serverLevel) || context.caster() == null) {
            return;
        }

        LivingEntity caster = context.caster();
        Vec3 origin = caster.getEyePosition(1.0F);
        Vec3 direction = caster.getViewVector(1.0F).normalize();

        double damageScale = RealmEffectivenessConfiguration.getGameplayMultiplier(caster, DAMAGE_REALM_EXPONENT);
        double radiusScale = Math.min(MAX_RADIUS_MULTIPLIER, RealmEffectivenessConfiguration.getGameplayMultiplier(caster, RADIUS_REALM_EXPONENT));
        double rangeScale = Math.min(MAX_RANGE_MULTIPLIER, RealmEffectivenessConfiguration.getGameplayMultiplier(caster, RANGE_REALM_EXPONENT));
        double resolvedDamage = config.damage().resolve(context.scaledValueContext()) * damageScale;
        int scaledRadius = Math.clamp((int) Math.round(config.radius() * radiusScale), 1, MAX_EFFECT_RADIUS);
        double scaledMaxDistance = Math.max(config.growthDistance(), config.maxDistance() * rangeScale);

        ProjectileState state = new ProjectileState();
        state.id = UUID.randomUUID();
        state.level = serverLevel;
        state.caster = caster;
        state.origin = origin;
        state.direction = direction;
        state.speed = Math.max(0.05D, config.projectileSpeed());
        state.startRadius = Math.max(0.05D, config.startRadius());
        state.targetRadius = scaledRadius;
        state.growthDistance = Math.max(0.5D, config.growthDistance());
        state.maxDistance = scaledMaxDistance;
        state.damage = resolvedDamage;
        state.destroyUnbreakable = config.destroyUnbreakable();
        state.soundRange = config.soundRange();

        ACTIVE.put(state.id, state);
        syncNearbyObservers(state);
    }

    private static SphericalProjectilePayload launchSnapshot(ProjectileState state) {
        return new SphericalProjectilePayload(
                state.id, state.level.dimension().identifier(), state.origin, state.direction,
                state.speed, state.startRadius, state.targetRadius, state.growthDistance,
                state.maxDistance, state.traveled, EFFECT_COLOR
        );
    }

    private static SphericalDestructionPayload impactSnapshot(ProjectileState state) {
        return new SphericalDestructionPayload(
                state.id, state.impactPos, (int) Math.round(state.targetRadius),
                state.impactTravelRadius, state.impactAgeTicks, state.soundRange,
                EFFECT_COLOR, state.level.dimension().identifier()
        );
    }

    private static void syncNearbyObservers(ProjectileState state) {
        Vec3 center = state.detonating ? state.impactPos : state.origin.add(state.direction.scale(state.traveled));
        double range = VISUAL_TRACKING_RANGE + (state.detonating ? state.targetRadius : 2.5D);
        double rangeSquared = range * range;
        SphericalProjectilePayload launchPacket = null;
        SphericalDestructionPayload impactPacket = null;

        for (ServerPlayer player : state.level.players()) {
            if (state.seenPlayers.contains(player.getUUID()) || player.position().distanceToSqr(center) > rangeSquared) {
                continue;
            }
            if (launchPacket == null) {
                launchPacket = launchSnapshot(state);
            }
            PacketDistributor.sendToPlayer(player, launchPacket);
            state.seenPlayers.add(player.getUUID());
            if (state.detonating) {
                if (impactPacket == null) {
                    impactPacket = impactSnapshot(state);
                }
                PacketDistributor.sendToPlayer(player, impactPacket);
            }
        }
    }

    private static void beginDetonation(ProjectileState state, Vec3 impactPos) {
        state.impactPos = impactPos;
        state.impactTravelRadius = travelRadius(state);
        state.detonating = true;
        state.impactAgeTicks = 0;
        SphericalDestructionPayload payload = impactSnapshot(state);
        for (ServerPlayer player : state.level.players()) {
            if (state.seenPlayers.contains(player.getUUID())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
        syncNearbyObservers(state);
    }

    private static float travelRadius(ProjectileState state) {
        double progress = Math.clamp(state.traveled / state.growthDistance, 0.0D, 1.0D);
        progress = progress * progress * (3.0D - 2.0D * progress);
        double radius = 0.65D + Math.sqrt(Math.max(state.targetRadius, 1.0D)) * 0.35D;
        radius = Math.min(2.5D, Math.max(state.startRadius, radius));
        return (float) (state.startRadius + (radius - state.startRadius) * progress);
    }

    private static void completeDetonation(ProjectileState state) {
        Vec3 impactPos = state.impactPos;
        if (impactPos == null) {
            return;
        }
        clearSphere(state.level, BlockPos.containing(impactPos), (int) Math.round(state.targetRadius), state.destroyUnbreakable);
        applyDamage(state.level, state.caster, impactPos, state.targetRadius, state.damage);
    }

    private static void applyDamage(ServerLevel level, LivingEntity caster, Vec3 center, double radius, double damage) {
        if (!Double.isFinite(damage) || damage <= 0.0D) {
            return;
        }
        double radiusSqr = radius * radius;
        for (var entity : level.getEntities(caster, new AABB(BlockPos.containing(center)).inflate(radius))) {
            if (entity.position().distanceToSqr(center) > radiusSqr) {
                continue;
            }
            if (entity instanceof LivingEntity living) {
                living.hurtServer(level, level.damageSources().generic(), (float) damage);
            }
        }
    }

    private static void clearSphere(ServerLevel level, BlockPos center, int radius, boolean destroyUnbreakable) {
        int radiusSqr = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radiusSqr) {
                        continue;
                    }
                    BlockPos pos = center.offset(x, y, z);
                    var blockState = level.getBlockState(pos);
                    if (blockState.isAir()) {
                        continue;
                    }
                    if (!destroyUnbreakable && blockState.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static final class ProjectileState {
        UUID id;
        ServerLevel level;
        LivingEntity caster;
        Vec3 origin;
        Vec3 direction;
        double speed;
        double traveled;
        double startRadius;
        double targetRadius;
        double growthDistance;
        double maxDistance;
        double damage;
        double soundRange;
        boolean destroyUnbreakable;
        boolean detonating;
        boolean destructionApplied;
        int impactAgeTicks;
        int syncTicks;
        Vec3 impactPos;
        float impactTravelRadius;
        final Set<UUID> seenPlayers = new HashSet<>();
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerTick {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Pre event) {
            if (ACTIVE.isEmpty()) {
                return;
            }
            for (var iterator = ACTIVE.entrySet().iterator(); iterator.hasNext(); ) {
                ProjectileState state = iterator.next().getValue();

                if (state.detonating) {
                    state.impactAgeTicks++;
                    if (!state.destructionApplied && state.impactAgeTicks >= DETONATION_DELAY_TICKS) {
                        completeDetonation(state);
                        state.destructionApplied = true;
                    }
                    if (state.impactAgeTicks >= IMPACT_VISUAL_TICKS) {
                        iterator.remove();
                        continue;
                    }
                } else {
                    Vec3 from = state.origin.add(state.direction.scale(state.traveled));
                    state.traveled = Math.min(state.maxDistance, state.traveled + state.speed);
                    Vec3 to = state.origin.add(state.direction.scale(state.traveled));
                    HitResult hit = state.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, state.caster));
                    if (hit instanceof BlockHitResult blockHit && hit.getType() != HitResult.Type.MISS) {
                        beginDetonation(state, blockHit.getLocation());
                    } else if (state.traveled >= state.maxDistance) {
                        beginDetonation(state, to);
                    }
                }
                if (++state.syncTicks % 4 == 0) {
                    syncNearbyObservers(state);
                }
            }
        }
    }
}
