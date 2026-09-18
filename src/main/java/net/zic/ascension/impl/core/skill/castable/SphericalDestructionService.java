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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.network.SphericalDestructionPayload;
import net.zic.ascension.network.SphericalProjectilePayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backing logic for SkillActions.SphericalDestruction. Launches a growing
 * rotating sphere that travels from the caster until it hits a block (or
 * runs out of range), then detonates a true-sphere destruction there — real
 * distance test, not the column-by-column vertical clear the orbital railgun
 * strike does.
 *
 * Networking is two payloads, not one:
 *  - SphericalProjectilePayload: sent ONCE at launch. Clients simulate the
 *    travel/growth themselves from origin+direction+speed, so there's no
 *    per-tick network spam.
 *  - SphericalDestructionPayload: sent ONCE at the real, server-authoritative
 *    impact, triggering the actual shockwave visual/sound/damage.
 *
 * TODO: PacketDistributor.sendToPlayer(...) is the standard NeoForge API for
 * this, but I don't have confirmation against your exact NeoForge version —
 * if this doesn't compile, tell me what the real method looks like.
 *
 * TODO: resolveRealmScale() is a stub returning 1.0. Wire this to whatever
 * gives you the caster's actual cultivation realm/power level — radius and
 * damage both multiply by it. I don't have that accessor; the closest thing
 * I've seen in your code is RealmEffectivenessConfiguration.getRelativeEffectiveness(attacker, target),
 * which compares TWO entities rather than giving one caster an absolute scale,
 * so it's not a direct fit here.
 */
public final class SphericalDestructionService {
    private SphericalDestructionService() {
    }

    private static final Map<UUID, ProjectileState> ACTIVE = new ConcurrentHashMap<>();
    private static final int WAVE_COLOR = 0xFF6A1B;

    /** Called by SkillActions.SphericalDestruction.apply() — fires the traveling sphere. */
    public static void launch(SkillActionContext context, SkillActions.SphericalDestruction config) {
        if (!(context.level() instanceof ServerLevel serverLevel) || context.caster() == null) {
            return;
        }

        LivingEntity caster = context.caster();
        Vec3 origin = caster.getEyePosition(1.0F);
        Vec3 direction = caster.getViewVector(1.0F).normalize();

        double realmScale = resolveRealmScale(caster);
        double resolvedDamage = config.damage().resolve(context.scaledValueContext()) * realmScale;
        int scaledRadius = Math.max(1, (int) Math.round(config.radius() * realmScale));

        ProjectileState state = new ProjectileState();
        state.level = serverLevel;
        state.caster = caster;
        state.origin = origin;
        state.direction = direction;
        state.speed = Math.max(0.05, config.projectileSpeed());
        state.startRadius = Math.max(0.05, config.startRadius());
        state.targetRadius = scaledRadius;
        state.growthDistance = Math.max(0.5, config.growthDistance());
        state.maxDistance = Math.max(state.growthDistance, config.maxDistance());
        state.damage = resolvedDamage;
        state.destroyUnbreakable = config.destroyUnbreakable();
        state.soundRange = config.soundRange();

        ACTIVE.put(UUID.randomUUID(), state);

        broadcastToNearby(serverLevel, origin, config.soundRange(), new SphericalProjectilePayload(
                origin,
                direction,
                state.speed,
                state.startRadius,
                state.targetRadius,
                state.growthDistance,
                state.maxDistance
        ));
    }

    /**
     * TODO stub — see class doc. Returning 1.0 means no scaling happens yet;
     * radius/damage will just be whatever the skill JSON says until this is
     * wired to a real realm lookup.
     */
    private static double resolveRealmScale(LivingEntity caster) {
        return 1.0;
    }

    private static void detonate(ProjectileState state, Vec3 impactPos) {
        clearSphere(state.level, BlockPos.containing(impactPos), (int) Math.round(state.targetRadius), state.destroyUnbreakable);
        applyDamage(state.level, state.caster, impactPos, state.targetRadius, state.damage);
        broadcastToNearby(state.level, impactPos, state.soundRange, new SphericalDestructionPayload(
                impactPos,
                (int) Math.round(state.targetRadius),
                state.level.dimension().identifier()
        ));
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

    /** True sphere via distance test — every block within `radius` of center. */
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
                    if (!destroyUnbreakable && blockState.is(Blocks.BEDROCK)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void broadcastToNearby(ServerLevel level, Vec3 center, double range, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        double rangeSqr = range * range;
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= rangeSqr) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static final class ProjectileState {
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
    }

    /** Server-authoritative travel + block collision, 20 times a second. */
    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerTick {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Pre event) {
            if (ACTIVE.isEmpty()) {
                return;
            }
            for (var iterator = ACTIVE.entrySet().iterator(); iterator.hasNext(); ) {
                ProjectileState state = iterator.next().getValue();
                Vec3 from = state.origin.add(state.direction.scale(state.traveled));
                state.traveled += state.speed;
                Vec3 to = state.origin.add(state.direction.scale(state.traveled));

                HitResult hit = state.level.clip(new ClipContext(
                        from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, state.caster
                ));

                if (hit instanceof BlockHitResult blockHit && hit.getType() != HitResult.Type.MISS) {
                    detonate(state, blockHit.getLocation());
                    iterator.remove();
                } else if (state.traveled >= state.maxDistance) {
                    detonate(state, to);
                    iterator.remove();
                }
            }
        }
    }
}