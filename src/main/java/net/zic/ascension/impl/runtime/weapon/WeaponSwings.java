package net.zic.ascension.impl.runtime.weapon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.Config;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.impl.core.damage.AscensionDamageService;
import net.zic.ascension.impl.core.effect.SkillEffectService;
import net.zic.ascension.impl.core.skill.passive.WeaponMasteryService;
import net.zic.ascension.impl.runtime.object.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class WeaponSwings {
    public static final Identifier VISUAL_ID = AscensionCraft.prefix("weapon_swing");
    private static final Identifier DEFAULT_DAMAGE_TYPE = Identifier.fromNamespaceAndPath("minecraft", "player_attack");
    private static final double DRAG = 0.92D;
    private static final List<Instance> ACTIVE = new ArrayList<>();

    private WeaponSwings() {
    }

    public static UUID spawn(
            ServerLevel level,
            LivingEntity owner,
            Vec3 position,
            float xRotation,
            float yRotation,
            WeaponSwingSpec spec
    ) {
        UUID runtimeId = UUID.randomUUID();
        long spawnedAt = level.getGameTime();
        Instance instance = new Instance(
                runtimeId,
                level.dimension(),
                owner.getUUID(),
                position,
                spec.movement(),
                xRotation,
                yRotation,
                spec,
                spawnedAt
        );
        ACTIVE.add(instance);
        RuntimeVisualSync.spawn(level, visualState(instance));
        return runtimeId;
    }

    public static boolean remove(UUID runtimeId) {
        return runtimeId != null && ACTIVE.removeIf(instance -> instance.runtimeId.equals(runtimeId));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Instance instance : List.copyOf(ACTIVE)) {
            ServerLevel level = event.getServer().getLevel(instance.dimension);
            if (level == null || !tick(level, instance)) {
                ACTIVE.remove(instance);
                if (level != null) {
                    RuntimeVisualSync.remove(level, visualState(instance));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
        WeaponMasteryService.clearRuntimeState();
    }

    private static boolean tick(ServerLevel level, Instance instance) {
        Entity entity = level.getEntity(instance.ownerId);
        if (!(entity instanceof LivingEntity owner) || owner.isRemoved()) {
            return false;
        }

        Vec3 nextPosition = instance.position.add(instance.velocity);
        BlockHitResult blockHit = findBlockImpact(level, owner, instance.position, nextPosition, instance.spec.blockImpact());
        boolean collided = blockHit != null;
        if (collided) {
            instance.position = blockHit.getLocation();
        }

        if (!instance.damageApplied) {
            if (instance.spec.usesRayHitDetection()) {
                applyRayDamage(level, owner, instance);
            } else {
                applyAreaDamage(level, owner, instance);
            }
            instance.damageApplied = instance.velocity.lengthSqr() < 1.0E-6D;
        }

        if (collided) {
            WeaponSwingSpec.BlockImpact impact = instance.spec.blockImpact();
            if (impact.mode() == WeaponSwingSpec.BlockImpact.Mode.BREAK) {
                applyBlockImpact(level, owner, instance, blockHit, impact);
            }
            if (impact.consumeProjection()) {
                return false;
            }
            instance.velocity = Vec3.ZERO;
            instance.damageApplied = true;
        } else {
            instance.position = nextPosition;
            instance.velocity = instance.velocity.scale(DRAG);
        }

        instance.ticksLived++;
        return instance.ticksLived < instance.spec.duration();
    }

    private static BlockHitResult findBlockImpact(
            ServerLevel level,
            LivingEntity owner,
            Vec3 start,
            Vec3 end,
            WeaponSwingSpec.BlockImpact impact
    ) {
        if (impact.mode() == WeaponSwingSpec.BlockImpact.Mode.IGNORE
                || start.distanceToSqr(end) <= 1.0E-10D) {
            return null;
        }
        BlockHitResult result = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                owner
        ));
        return result.getType() == HitResult.Type.BLOCK ? result : null;
    }

    private static void applyAreaDamage(ServerLevel level, LivingEntity owner, Instance instance) {
        Vec3 radius = instance.spec.radius();
        double average = (radius.x + radius.y + radius.z) / 5.0D;
        AABB search = new AABB(
                instance.position.x - 0.5D,
                instance.position.y,
                instance.position.z - 0.5D,
                instance.position.x + 0.5D,
                instance.position.y + 1.0D,
                instance.position.z + 0.5D
        ).inflate(average);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                candidate -> validTarget(owner, candidate, instance)
        )) {
            applyKnockback(owner, target, instance.position, instance.spec.knockback());
            applyDamage(level, owner, target, instance);
            instance.hitEntities.add(target.getUUID());
        }
    }

    private static void applyRayDamage(ServerLevel level, LivingEntity owner, Instance instance) {
        Vec3 direction = direction(instance.xRotation, instance.yRotation);
        double halfLength = instance.spec.radius().z * 0.5D;
        Vec3 rayStart = instance.position.add(direction.scale(halfLength));
        Vec3 rayEnd = instance.position.add(direction.scale(-halfLength));
        AABB search = new AABB(rayStart, rayEnd).inflate(Math.max(0.2D, instance.spec.radius().x * 0.5D));

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                search,
                candidate -> validTarget(owner, candidate, instance)
        )) {
            if (target.getBoundingBox().inflate(0.15D).clip(rayStart, rayEnd).isEmpty()) {
                continue;
            }
            applyKnockback(owner, target, instance.position, instance.spec.knockback());
            applyDamage(level, owner, target, instance);
            instance.hitEntities.add(target.getUUID());
        }
    }

    private static void applyDamage(
            ServerLevel level,
            LivingEntity owner,
            LivingEntity target,
            Instance instance
    ) {
        if (instance.spec.damage() <= 0.0D) {
            return;
        }
        SkillActionContext context = new SkillActionContext(
                level,
                owner,
                instance.spec.skillId(),
                target,
                target.getBoundingBox().getCenter(),
                0.0D,
                java.util.Map.of()
        );
        boolean damaged = AscensionDamageService.apply(
                context,
                instance.spec.damage(),
                DEFAULT_DAMAGE_TYPE,
                new LinkedHashSet<>(instance.spec.classifications()),
                instance.spec.path(),
                instance.spec.technique()
        );
        if (!damaged) {
            return;
        }

        target.invulnerableTime = instance.velocity.lengthSqr() < 1.0E-6D ? 0 : 8;
        instance.spec.hitEffect().ifPresent(effect -> SkillEffectService.apply(
                target,
                effect.definition(),
                owner.getUUID(),
                instance.spec.skillId(),
                effect.duration(),
                effect.potency()
        ));
    }

    private static void applyBlockImpact(
            ServerLevel level,
            LivingEntity owner,
            Instance instance,
            BlockHitResult hit,
            WeaponSwingSpec.BlockImpact impact
    ) {
        if (!(owner instanceof ServerPlayer player)
                || !Config.COMMON.WEAPON_PROJECTION_BLOCK_BREAKING.get()
                || impact.maximumBlocks() <= 0
                || impact.radius() <= 0.0D) {
            return;
        }

        Optional<TagKey<Block>> allowed = impact.allowedTag().map(id -> TagKey.create(Registries.BLOCK, id));
        Optional<TagKey<Block>> blocked = impact.blockedTag().map(id -> TagKey.create(Registries.BLOCK, id));
        List<BlockCandidate> candidates = blockCandidates(
                hit.getBlockPos(),
                instance.velocity,
                impact.radius(),
                impact.depth()
        );

        int broken = 0;
        for (BlockCandidate candidate : candidates) {
            if (broken >= impact.maximumBlocks()) {
                break;
            }
            BlockPos position = candidate.position();
            BlockState state = level.getBlockState(position);
            if (state.isAir() || state.hasBlockEntity()) {
                continue;
            }
            if (blocked.isPresent() && state.is(blocked.get())) {
                continue;
            }
            if (allowed.isPresent() && !state.is(allowed.get())) {
                continue;
            }
            float hardness = state.getDestroySpeed(level, position);
            if (hardness < 0.0F || hardness > impact.maximumHardness()) {
                continue;
            }
            if (player.gameMode.destroyBlock(position)) {
                broken++;
            }
        }
    }

    private static List<BlockCandidate> blockCandidates(
            BlockPos center,
            Vec3 movement,
            double radius,
            int depth
    ) {
        int range = Math.max(0, (int) Math.ceil(radius));
        Axis axis = Axis.dominant(movement);
        int direction = axis.direction(movement);
        Set<BlockPos> unique = new HashSet<>();
        List<BlockCandidate> candidates = new ArrayList<>();

        for (int layer = 0; layer < depth; layer++) {
            for (int first = -range; first <= range; first++) {
                for (int second = -range; second <= range; second++) {
                    if (first * first + second * second > radius * radius + 0.25D) {
                        continue;
                    }
                    BlockPos position = axis.offset(center, first, second, layer * direction).immutable();
                    if (unique.add(position)) {
                        candidates.add(new BlockCandidate(
                                position,
                                layer * layer + first * first + second * second
                        ));
                    }
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(BlockCandidate::distance));
        return candidates;
    }

    private static void applyKnockback(
            LivingEntity owner,
            LivingEntity target,
            Vec3 origin,
            double knockback
    ) {
        if (knockback <= 0.0D || target.invulnerableTime != 0) {
            return;
        }
        Vec3 direction = target.position().subtract(origin);
        if (direction.lengthSqr() <= 1.0E-8D) {
            direction = target.position().subtract(owner.position());
        }
        if (direction.lengthSqr() <= 1.0E-8D) {
            direction = owner.getLookAngle();
        }
        direction = direction.normalize().scale(knockback);
        target.setDeltaMovement(direction.x, 0.3D * knockback, direction.z);
        target.hurtMarked = true;
    }

    private static boolean validTarget(LivingEntity owner, LivingEntity target, Instance instance) {
        return target != owner
                && !target.isRemoved()
                && target.isAlive()
                && !target.isAlliedTo(owner)
                && !instance.hitEntities.contains(target.getUUID());
    }

    private static Vec3 direction(float xRotation, float yRotation) {
        double pitch = Math.toRadians(xRotation);
        double yaw = Math.toRadians(yRotation);
        double horizontal = Math.cos(pitch);
        return new Vec3(
                -Math.sin(yaw) * horizontal,
                -Math.sin(pitch),
                Math.cos(yaw) * horizontal
        ).normalize();
    }

    private static RuntimeVisualState visualState(Instance instance) {
        Identifier textureBase = VfxColorRegistry.textureBase(
                instance.spec.vfxType(),
                instance.spec.colorFolder()
        );
        Vec3 radius = instance.spec.radius();
        RuntimeVisualDefinition.Element layer = new RuntimeVisualDefinition.Element(
                RuntimeVisualDefinition.Types.SPRITE,
                RuntimeVisualDefinition.PositionMode.ORIGIN,
                new RuntimeVisualDefinition.Transform(
                        Vec3.ZERO,
                        new Vec3(instance.xRotation, instance.yRotation, instance.spec.rotationZ()),
                        RuntimeVisualDefinition.VisualValue.constant(1.0D)
                ),
                new RuntimeVisualDefinition.Appearance(
                        new RuntimeVisualDefinition.VisualColor(255, 255, 255, 255),
                        new RuntimeVisualDefinition.VisualColor(255, 255, 255, 255),
                        1.0F,
                        true,
                        false
                ),
                new RuntimeVisualDefinition.Geometry(
                        RuntimeVisualDefinition.VisualValue.constant(radius.x),
                        RuntimeVisualDefinition.VisualValue.constant(0.0D),
                        RuntimeVisualDefinition.VisualValue.constant(radius.y),
                        RuntimeVisualDefinition.VisualValue.constant(radius.x),
                        RuntimeVisualDefinition.VisualValue.constant(radius.z),
                        4,
                        1
                ),
                RuntimeVisualDefinition.Motion.DEFAULT,
                new RuntimeVisualDefinition.Resources(
                        Optional.of(textureBase),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        7,
                        1
                )
        );
        return new RuntimeVisualState(
                instance.runtimeId,
                VISUAL_ID,
                instance.ownerId,
                instance.initialPosition,
                instance.initialVelocity,
                List.of(),
                List.of(),
                instance.spawnedAt + instance.spec.duration(),
                0,
                0,
                0.0F,
                instance.runtimeId.getMostSignificantBits(),
                instance.spawnedAt,
                instance.spec.duration(),
                new RuntimeVisualDefinition(List.of(layer))
        );
    }

    private enum Axis {
        X,
        Y,
        Z;

        private static Axis dominant(Vec3 movement) {
            double x = Math.abs(movement.x);
            double y = Math.abs(movement.y);
            double z = Math.abs(movement.z);
            if (y >= x && y >= z) {
                return Y;
            }
            return x >= z ? X : Z;
        }

        private int direction(Vec3 movement) {
            double value = switch (this) {
                case X -> movement.x;
                case Y -> movement.y;
                case Z -> movement.z;
            };
            return value < 0.0D ? -1 : 1;
        }

        private BlockPos offset(BlockPos center, int first, int second, int depth) {
            return switch (this) {
                case X -> center.offset(depth, first, second);
                case Y -> center.offset(first, depth, second);
                case Z -> center.offset(first, second, depth);
            };
        }
    }

    private record BlockCandidate(BlockPos position, double distance) {
    }

    private static final class Instance {
        private final UUID runtimeId;
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Vec3 initialPosition;
        private final Vec3 initialVelocity;
        private final float xRotation;
        private final float yRotation;
        private final WeaponSwingSpec spec;
        private final long spawnedAt;
        private final Set<UUID> hitEntities = new HashSet<>();
        private Vec3 position;
        private Vec3 velocity;
        private int ticksLived;
        private boolean damageApplied;

        private Instance(
                UUID runtimeId,
                ResourceKey<Level> dimension,
                UUID ownerId,
                Vec3 position,
                Vec3 velocity,
                float xRotation,
                float yRotation,
                WeaponSwingSpec spec,
                long spawnedAt
        ) {
            this.runtimeId = runtimeId;
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.initialPosition = position;
            this.initialVelocity = velocity;
            this.position = position;
            this.velocity = velocity;
            this.xRotation = xRotation;
            this.yRotation = yRotation;
            this.spec = spec;
            this.spawnedAt = spawnedAt;
        }
    }
}
