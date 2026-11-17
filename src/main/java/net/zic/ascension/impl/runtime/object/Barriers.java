package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.runtime.projectile.ProjectileImpactResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class Barriers {
    public static final Identifier ABSORBED = AscensionCraft.prefix("barrier_absorbed");
    public static final Identifier REMAINING_DAMAGE = AscensionCraft.prefix("barrier_remaining_damage");
    public static final Identifier DURABILITY = AscensionCraft.prefix("barrier_durability");
    public static final Identifier MAXIMUM_DURABILITY = AscensionCraft.prefix("barrier_maximum_durability");
    private static final List<BarrierInstance> ACTIVE = new ArrayList<>();
    // Entities whose hit being dealt now was taken whole by a barrier with suppress_hurt_effects, so LivingEntityBarrierHurtMixin
    // leaves out the hurt flash, sound and knockback that follow it. Cleared as each hit starts and ends.
    private static final Set<LivingEntity> HURT_EFFECTS_SUPPRESSED = Collections.newSetFromMap(new WeakHashMap<>());
    // How hard a solid barrier pushes a player back out (blocks a tick per block they're in, plus a little), since a player's
    // position can only be moved from their own client.
    private static final double PLAYER_PUSH = 0.5D;
    private static final double PLAYER_PUSH_MIN = 0.05D;

    private Barriers() {
    }

    public static UUID apply(SkillActionContext context, LivingEntity protectedEntity, Identifier definitionId) {
        if (context == null || protectedEntity == null || definitionId == null || protectedEntity.isRemoved()) {
            return null;
        }
        BarrierDefinition definition = SkillDefinitions.resolveStored(
                BarrierDefinition.class,
                context.skill(),
                definitionId,
                CoreRegistries.BARRIER_REGISTRY,
                context.level().registryAccess()
        );
        if (definition == null) {
            return null;
        }

        double duration = definition.duration().resolve(context.scaledValueContext());
        double durability = definition.durability().resolve(context.scaledValueContext());
        double absorption = definition.absorption().resolve(context.scaledValueContext());
        if (!Double.isFinite(duration) || duration <= 0.0D
                || !Double.isFinite(durability) || durability <= 0.0D
                || !Double.isFinite(absorption)) {
            return null;
        }

        if (definition.replaceExisting()) {
            removeMatching(
                    context.level(),
                    context.caster().getUUID(),
                    protectedEntity.getUUID(),
                    definitionId,
                    Removal.EXPLICIT
            );
        }

        double radius = 1.15D;
        double height = Math.max(1.0D, protectedEntity.getBbHeight() + 0.35D);
        if (definition.visual().isPresent()) {
            radius = definition.visual().get().radius().resolve(context.scaledValueContext());
            height = definition.visual().get().height().resolve(context.scaledValueContext());
        }
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            radius = 1.15D;
        }
        if (!Double.isFinite(height) || height <= 0.0D) {
            height = Math.max(1.0D, protectedEntity.getBbHeight() + 0.35D);
        }

        BarrierInstance barrier = new BarrierInstance(
                context.level().dimension(),
                context.caster().getUUID(),
                protectedEntity.getUUID(),
                context.skill(),
                definitionId,
                context.charge(),
                context.variables(),
                durability,
                Math.clamp(absorption, 0.0D, 1.0D),
                definition.overflow(),
                definition.overflowMultiplier(),
                definition.priority(),
                context.level().getGameTime() + Math.max(1L, Math.round(duration)),
                protectedEntity.position(),
                radius,
                height
        );
        ACTIVE.add(barrier);
        syncVisual(context.level(), barrier, definition, true);
        return barrier.runtimeId;
    }

    public static double repair(
            ServerLevel level,
            UUID ownerId,
            UUID protectedEntityId,
            Identifier definitionId,
            double amount
    ) {
        if (level == null || ownerId == null || protectedEntityId == null || definitionId == null
                || !Double.isFinite(amount) || amount <= 0.0D) {
            return 0.0D;
        }
        double restored = 0.0D;
        for (BarrierInstance barrier : ACTIVE) {
            if (!barrier.dimension.equals(level.dimension())
                    || !barrier.ownerId.equals(ownerId)
                    || !barrier.protectedEntityId.equals(protectedEntityId)
                    || !barrier.definitionId.equals(definitionId)) {
                continue;
            }
            BarrierDefinition definition = SkillDefinitions.resolveStored(
                    BarrierDefinition.class,
                    barrier.skillId,
                    barrier.definitionId,
                    CoreRegistries.BARRIER_REGISTRY,
                    level.registryAccess()
            );
            if (definition == null) {
                continue;
            }
            double previous = barrier.durability;
            barrier.durability = Math.min(barrier.maximumDurability, barrier.durability + amount);
            restored += barrier.durability - previous;
            syncVisual(level, barrier, definition, true);
        }
        return restored;
    }

    public static int removeMatching(
            ServerLevel level,
            UUID ownerId,
            UUID protectedEntityId,
            Identifier definitionId,
            Removal removal
    ) {
        if (level == null || ownerId == null || protectedEntityId == null) {
            return 0;
        }
        int removed = 0;
        Iterator<BarrierInstance> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            BarrierInstance barrier = iterator.next();
            if (!barrier.dimension.equals(level.dimension())
                    || !barrier.ownerId.equals(ownerId)
                    || !barrier.protectedEntityId.equals(protectedEntityId)
                    || definitionId != null && !barrier.definitionId.equals(definitionId)) {
                continue;
            }
            iterator.remove();
            finish(level, barrier, removal);
            removed++;
        }
        return removed;
    }

    public static void applyDamage(RPGEngineEntityDamagedEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        double remaining = event.getDamage();
        if (!Double.isFinite(remaining) || remaining <= 0.0D) {
            return;
        }

        List<BarrierInstance> candidates = ACTIVE.stream()
                .filter(barrier -> barrier.dimension.equals(level.dimension()))
                .filter(barrier -> barrier.protectedEntityId.equals(event.getEntity().getUUID()))
                .sorted(Comparator.comparingInt((BarrierInstance value) -> value.priority).reversed())
                .toList();

        boolean suppressHurtEffects = false;
        for (BarrierInstance barrier : candidates) {
            if (remaining <= 0.0D) {
                break;
            }
            if (!ACTIVE.contains(barrier)) {
                continue;
            }
            BarrierDefinition definition = SkillDefinitions.resolveStored(BarrierDefinition.class, barrier.skillId, barrier.definitionId, CoreRegistries.BARRIER_REGISTRY, level.registryAccess());
            if (definition == null || !definition.filter().accepts(event.getSource())) {
                continue;
            }

            double requested = remaining * barrier.absorption;
            double absorbed = Math.min(barrier.durability, requested);
            if (absorbed <= 0.0D) {
                continue;
            }

            barrier.durability = Math.max(0.0D, barrier.durability - absorbed);
            suppressHurtEffects |= definition.suppressHurtEffects();
            remaining = Math.max(0.0D, remaining - (barrier.overflow ? absorbed : requested));
            if (remaining <= 0.0D) {
                ProjectileImpactResponses.apply(event.getSource(), event.getEntity(), definition.projectileResponse());
            }
            execute(level, barrier, definition.onAbsorb(), event.getEntity(), absorbed, remaining);
            if (!ACTIVE.contains(barrier)) {
                continue;
            }

            if (barrier.durability <= 0.0D) {
                // What gets past a barrier as it breaks is cut by its overflow_multiplier.
                remaining *= barrier.overflowMultiplier;
                ACTIVE.remove(barrier);
                finish(level, barrier, Removal.BROKEN);
            } else {
                syncVisual(level, barrier, definition, true);
            }
        }

        if (suppressHurtEffects && remaining <= 0.0D) {
            HURT_EFFECTS_SUPPRESSED.add(event.getEntity());
        }
        event.resolveDamage(AscensionCraft.prefix("damage_barriers"), 60, remaining);
    }

    /**
     * Whether the hit being dealt to this entity now was taken whole by a barrier with suppress_hurt_effects
     * (LivingEntityBarrierHurtMixin).
     */
    public static boolean hurtEffectsSuppressed(LivingEntity entity) {
        return HURT_EFFECTS_SUPPRESSED.contains(entity);
    }

    public static void clearHurtEffectsSuppressed(LivingEntity entity) {
        HURT_EFFECTS_SUPPRESSED.remove(entity);
    }

    /**
     * A hit brought to nothing before it reached the barriers (by a passive's defence, say): if the entity has a barrier with
     * suppress_hurt_effects that would have taken it, it leaves out the hurt effects too, as if the barrier had.
     */
    public static void noteResolvedHit(RPGEngineEntityDamagedEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        for (BarrierInstance barrier : ACTIVE) {
            if (!barrier.dimension.equals(level.dimension()) || !barrier.protectedEntityId.equals(event.getEntity().getUUID())) {
                continue;
            }
            BarrierDefinition definition = SkillDefinitions.resolveStored(BarrierDefinition.class, barrier.skillId, barrier.definitionId, CoreRegistries.BARRIER_REGISTRY, level.registryAccess());
            if (definition != null && definition.suppressHurtEffects() && definition.filter().accepts(event.getSource())) {
                HURT_EFFECTS_SUPPRESSED.add(event.getEntity());
                return;
            }
        }
    }

    /**
     * A solid barrier pushes every other living thing overlapping it (its radius and height round the middle of its entity,
     * widened by theirs) back out to its surface and stops it moving further in. Mobs are moved straight out (colliding with
     * blocks on the way); players are given a shove, as only their own client can move them.
     */
    private static void pushOut(ServerLevel level, BarrierInstance barrier, LivingEntity protectedEntity) {
        double halfHeight = barrier.height * 0.5D;
        Vec3 centre = barrier.position.add(0.0D, protectedEntity.getBbHeight() * 0.5D, 0.0D);
        AABB area = new AABB(centre, centre).inflate(barrier.radius + 1.0D, halfHeight + 1.5D, barrier.radius + 1.0D);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (other == protectedEntity || other.getUUID().equals(barrier.ownerId) || other.isSpectator() || !other.isAlive()) {
                continue;
            }
            double otherHalfWidth = other.getBbWidth() * 0.5D;
            double otherHalfHeight = other.getBbHeight() * 0.5D;
            double reachX = barrier.radius + otherHalfWidth;
            double reachY = halfHeight + otherHalfHeight;
            Vec3 offset = other.position().add(0.0D, otherHalfHeight, 0.0D).subtract(centre);
            // How far out it is, 1 on the surface, in the barrier's ellipsoid widened by the other's size.
            double x = offset.x / reachX;
            double y = offset.y / reachY;
            double z = offset.z / reachX;
            double depth = Math.sqrt(x * x + y * y + z * z);
            if (depth >= 1.0D) {
                continue;
            }
            if (depth < 1.0E-4D) {
                x = 1.0D;
                depth = 1.0D;
                y = 0.0D;
                z = 0.0D;
            }
            Vec3 surface = new Vec3(x / depth * reachX, y / depth * reachY, z / depth * reachX);
            Vec3 outward = new Vec3(x / reachX, y / reachY, z / reachX).normalize();
            Vec3 motion = other.getDeltaMovement();
            double inward = motion.dot(outward);
            if (inward < 0.0D) {
                motion = motion.subtract(outward.scale(inward));
            }
            if (other instanceof Player) {
                double push = PLAYER_PUSH * surface.subtract(offset).length() + PLAYER_PUSH_MIN;
                other.setDeltaMovement(motion.add(outward.scale(push)));
                other.hurtMarked = true;
            } else {
                other.setDeltaMovement(motion);
                other.move(MoverType.PISTON, surface.subtract(offset));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<BarrierInstance> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            BarrierInstance barrier = iterator.next();
            ServerLevel level = event.getServer().getLevel(barrier.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            Entity entity = level.getEntity(barrier.protectedEntityId);
            BarrierDefinition definition = SkillDefinitions.resolveStored(BarrierDefinition.class, barrier.skillId, barrier.definitionId, CoreRegistries.BARRIER_REGISTRY, level.registryAccess());
            if (!(entity instanceof LivingEntity living) || living.isRemoved() || !living.isAlive() || definition == null) {
                iterator.remove();
                finish(level, barrier, Removal.EXPLICIT);
                continue;
            }
            if (level.getGameTime() >= barrier.expiresAt) {
                iterator.remove();
                finish(level, barrier, Removal.EXPIRED);
                continue;
            }

            barrier.position = living.position();
            if (definition.solid()) {
                pushOut(level, barrier, living);
            }
            boolean moved = barrier.position.distanceToSqr(barrier.syncedPosition) > 0.0025D;
            boolean heartbeat = level.getGameTime() - barrier.lastVisualSync >= 20L;
            if (heartbeat || moved && level.getGameTime() - barrier.lastPositionSync >= 2L) {
                syncVisual(level, barrier, definition, true);
                barrier.lastPositionSync = level.getGameTime();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private static void finish(ServerLevel level, BarrierInstance barrier, Removal removal) {
        BarrierDefinition definition = SkillDefinitions.resolveStored(BarrierDefinition.class, barrier.skillId, barrier.definitionId, CoreRegistries.BARRIER_REGISTRY, level.registryAccess());
        removeVisual(level, barrier);
        if (definition == null) {
            return;
        }
        Entity entity = level.getEntity(barrier.protectedEntityId);
        if (!(entity instanceof LivingEntity target)) {
            return;
        }
        if (removal == Removal.BROKEN) {
            execute(level, barrier, definition.onBreak(), target, 0.0D, 0.0D);
        } else if (removal == Removal.EXPIRED) {
            execute(level, barrier, definition.onExpire(), target, 0.0D, 0.0D);
        }
    }

    private static void execute(
            ServerLevel level,
            BarrierInstance barrier,
            List<SkillAction> actions,
            LivingEntity target,
            double absorbed,
            double remaining
    ) {
        if (actions.isEmpty()) {
            return;
        }
        Entity owner = level.getEntity(barrier.ownerId);
        if (!(owner instanceof LivingEntity caster)) {
            return;
        }
        Map<Identifier, Double> variables = new LinkedHashMap<>(barrier.variables);
        variables.put(ABSORBED, absorbed);
        variables.put(REMAINING_DAMAGE, remaining);
        variables.put(DURABILITY, barrier.durability);
        variables.put(MAXIMUM_DURABILITY, barrier.maximumDurability);
        SkillActionContext context = new SkillActionContext(
                level,
                caster,
                barrier.skillId,
                target,
                target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                barrier.charge,
                variables,
                SkillActionAttribution.direct(caster)
        );
        for (SkillAction action : actions) {
            action.apply(context);
        }
    }

    private static void syncVisual(
            ServerLevel level,
            BarrierInstance barrier,
            BarrierDefinition definition,
            boolean force
    ) {
        if (definition.visual().isEmpty()) {
            removeVisual(level, barrier);
            return;
        }
        SkillActionContext context = context(level, barrier);
        if (context == null) {
            return;
        }
        VisualSelection selection = selectVisual(context, definition.visual().get(), barrier);
        if (selection == null) {
            removeVisual(level, barrier);
            return;
        }
        boolean changed = barrier.visualId == null
                || !barrier.visualId.equals(selection.id())
                || barrier.visualStage != selection.stage()
                || Math.abs(barrier.syncedDurability - barrier.durability) >= Math.max(0.5D, barrier.maximumDurability * 0.025D)
                || barrier.position.distanceToSqr(barrier.syncedPosition) > 0.0025D;
        if (!force && !changed) {
            return;
        }

        RuntimeVisualState state = visualState(barrier, selection.id(), selection.stage(), barrier.expiresAt);
        if (barrier.visualId == null) {
            RuntimeVisualSync.spawn(level, state);
        } else if (!barrier.visualId.equals(selection.id())) {
            RuntimeVisualSync.remove(level, visualState(
                    barrier,
                    barrier.visualId,
                    barrier.visualStage,
                    0L
            ));
            RuntimeVisualSync.spawn(level, state);
        } else {
            RuntimeVisualSync.update(level, state);
        }
        barrier.visualId = selection.id();
        barrier.visualStage = selection.stage();
        barrier.syncedDurability = barrier.durability;
        barrier.syncedPosition = barrier.position;
        barrier.lastVisualSync = level.getGameTime();
    }

    private static void removeVisual(ServerLevel level, BarrierInstance barrier) {
        if (barrier.visualId == null) {
            return;
        }
        RuntimeVisualSync.remove(level, visualState(
                barrier,
                barrier.visualId,
                barrier.visualStage,
                0L
        ));
        barrier.visualId = null;
    }

    private static VisualSelection selectVisual(
            SkillActionContext context,
            BarrierDefinition.Visual visual,
            BarrierInstance barrier
    ) {
        double fraction = barrier.maximumDurability <= 0.0D ? 0.0D : barrier.durability / barrier.maximumDurability;
        for (int index = 0; index < visual.stages().size(); index++) {
            BarrierDefinition.VisualStage stage = visual.stages().get(index);
            if (fraction <= stage.maximumDurabilityFraction()) {
                Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, stage.visual());
                return resolved == null ? null : new VisualSelection(resolved.id(), index + 1);
            }
        }
        Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, visual.id());
        return resolved == null ? null : new VisualSelection(resolved.id(), 0);
    }

    private static SkillActionContext context(ServerLevel level, BarrierInstance barrier) {
        Entity owner = level.getEntity(barrier.ownerId);
        Entity protectedEntity = level.getEntity(barrier.protectedEntityId);
        if (!(owner instanceof LivingEntity caster) || !(protectedEntity instanceof LivingEntity target)) {
            return null;
        }
        return new SkillActionContext(
                level,
                caster,
                barrier.skillId,
                target,
                target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                barrier.charge,
                barrier.variables,
                SkillActionAttribution.direct(caster)
        );
    }

    private static RuntimeVisualState visualState(
            BarrierInstance barrier,
            Identifier visual,
            int stage,
            long expiresAt
    ) {
        float progress = barrier.maximumDurability <= 0.0D
                ? 0.0F
                : (float) Math.clamp(barrier.durability / barrier.maximumDurability, 0.0D, 1.0D);
        return new RuntimeVisualState(
                barrier.runtimeId,
                visual,
                barrier.protectedEntityId,
                barrier.position,
                Vec3.ZERO,
                List.of(),
                List.of(),
                expiresAt,
                stage,
                0,
                progress,
                barrier.runtimeId.getMostSignificantBits(),
                barrier.radius,
                barrier.height
        );
    }

    private record VisualSelection(Identifier id, int stage) {
    }

    public enum Removal {
        EXPLICIT,
        BROKEN,
        EXPIRED
    }


    private static final class BarrierInstance {
        private final UUID runtimeId = UUID.randomUUID();
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final UUID protectedEntityId;
        private final Identifier skillId;
        private final Identifier definitionId;
        private final double charge;
        private final Map<Identifier, Double> variables;
        private final double maximumDurability;
        private final double absorption;
        private final boolean overflow;
        private final double overflowMultiplier;
        private final int priority;
        private final long expiresAt;
        private final double radius;
        private final double height;
        private double durability;
        private Vec3 position;
        private Vec3 syncedPosition;
        private Identifier visualId;
        private int visualStage;
        private double syncedDurability;
        private long lastPositionSync;
        private long lastVisualSync;

        private BarrierInstance(
                ResourceKey<Level> dimension,
                UUID ownerId,
                UUID protectedEntityId,
                Identifier skillId,
                Identifier definitionId,
                double charge,
                Map<Identifier, Double> variables,
                double durability,
                double absorption,
                boolean overflow,
                double overflowMultiplier,
                int priority,
                long expiresAt,
                Vec3 position,
                double radius,
                double height
        ) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.protectedEntityId = protectedEntityId;
            this.skillId = skillId;
            this.definitionId = definitionId;
            this.charge = charge;
            this.variables = variables == null ? Map.of() : Map.copyOf(variables);
            this.maximumDurability = durability;
            this.durability = durability;
            this.absorption = absorption;
            this.overflow = overflow;
            this.overflowMultiplier = overflowMultiplier;
            this.priority = priority;
            this.expiresAt = expiresAt;
            this.position = position;
            this.syncedPosition = position;
            this.radius = radius;
            this.height = height;
        }
    }
}
