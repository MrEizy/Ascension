package net.zic.ascension.impl.runtime.barrier;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.runtime.visual.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class Barriers {
    public static final Identifier ABSORBED = AscensionCraft.prefix("barrier_absorbed");
    public static final Identifier REMAINING_DAMAGE = AscensionCraft.prefix("barrier_remaining_damage");
    public static final Identifier DURABILITY = AscensionCraft.prefix("barrier_durability");
    public static final Identifier MAXIMUM_DURABILITY = AscensionCraft.prefix("barrier_maximum_durability");
    private static final List<BarrierInstance> ACTIVE = new ArrayList<>();

    private Barriers() {
    }

    public static UUID apply(SkillExecutionContext context, LivingEntity protectedEntity, Identifier definitionId) {
        if (context == null || protectedEntity == null || definitionId == null || protectedEntity.isRemoved()) {
            return null;
        }
        BarrierDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.BARRIER_REGISTRY,
                definitionId,
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
        BarrierDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.BARRIER_REGISTRY,
                definitionId,
                level.registryAccess()
        );
        if (definition == null) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
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

        for (BarrierInstance barrier : candidates) {
            if (remaining <= 0.0D) {
                break;
            }
            if (!ACTIVE.contains(barrier)) {
                continue;
            }
            BarrierDefinition definition = CoreRegistries.safeAccess(
                    CoreRegistries.BARRIER_REGISTRY,
                    barrier.definitionId,
                    level.registryAccess()
            );
            if (definition == null || !definition.filter().accepts(event.getSource())) {
                continue;
            }

            double requested = remaining * barrier.absorption;
            double absorbed = Math.min(barrier.durability, requested);
            if (absorbed <= 0.0D) {
                continue;
            }

            barrier.durability = Math.max(0.0D, barrier.durability - absorbed);
            remaining = Math.max(0.0D, remaining - (barrier.overflow ? absorbed : requested));
            execute(level, barrier, definition.onAbsorb(), event.getEntity(), absorbed, remaining);
            if (!ACTIVE.contains(barrier)) {
                continue;
            }

            if (barrier.durability <= 0.0D) {
                ACTIVE.remove(barrier);
                finish(level, barrier, Removal.BROKEN);
            } else {
                syncVisual(level, barrier, definition, true);
            }
        }

        event.setDamage(remaining);
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
            BarrierDefinition definition = CoreRegistries.safeAccess(
                    CoreRegistries.BARRIER_REGISTRY,
                    barrier.definitionId,
                    level.registryAccess()
            );
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
        BarrierDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.BARRIER_REGISTRY,
                barrier.definitionId,
                level.registryAccess()
        );
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
            List<SkillExecutionFeature> features,
            LivingEntity target,
            double absorbed,
            double remaining
    ) {
        if (features.isEmpty()) {
            return;
        }
        Entity owner = level.getEntity(barrier.ownerId);
        if (!(owner instanceof ServerPlayer caster)) {
            return;
        }
        Map<Identifier, Double> variables = new LinkedHashMap<>(barrier.variables);
        variables.put(ABSORBED, absorbed);
        variables.put(REMAINING_DAMAGE, remaining);
        variables.put(DURABILITY, barrier.durability);
        variables.put(MAXIMUM_DURABILITY, barrier.maximumDurability);
        SkillExecutionContext context = new SkillExecutionContext(
                level,
                caster,
                barrier.skillId,
                target,
                target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                barrier.charge,
                variables,
                SkillExecutionAttribution.direct(caster)
        );
        for (SkillExecutionFeature feature : features) {
            feature.apply(context);
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
        BarrierDefinition.Visual visual = definition.visual().get();
        VisualSelection selection = selectVisual(visual, barrier);
        boolean changed = barrier.visualId == null
                || !barrier.visualId.equals(selection.id)
                || barrier.visualStage != selection.stage
                || Math.abs(barrier.syncedDurability - barrier.durability) >= Math.max(0.5D, barrier.maximumDurability * 0.025D)
                || barrier.position.distanceToSqr(barrier.syncedPosition) > 0.0025D;
        if (!force && !changed) {
            return;
        }

        RuntimeVisualState state = visualState(barrier, selection.id, selection.stage, barrier.expiresAt);
        if (barrier.visualId == null) {
            RuntimeVisualSync.spawn(level, state);
        } else if (!barrier.visualId.equals(selection.id)) {
            RuntimeVisualSync.remove(level, visualState(barrier, barrier.visualId, barrier.visualStage, 0L));
            RuntimeVisualSync.spawn(level, state);
        } else {
            RuntimeVisualSync.update(level, state);
        }
        barrier.visualId = selection.id;
        barrier.visualStage = selection.stage;
        barrier.syncedDurability = barrier.durability;
        barrier.syncedPosition = barrier.position;
        barrier.lastVisualSync = level.getGameTime();
    }

    private static void removeVisual(ServerLevel level, BarrierInstance barrier) {
        if (barrier.visualId == null) {
            return;
        }
        RuntimeVisualSync.remove(level, visualState(barrier, barrier.visualId, barrier.visualStage, 0L));
        barrier.visualId = null;
    }

    private static VisualSelection selectVisual(BarrierDefinition.Visual visual, BarrierInstance barrier) {
        double fraction = barrier.maximumDurability <= 0.0D ? 0.0D : barrier.durability / barrier.maximumDurability;
        for (int index = 0; index < visual.stages().size(); index++) {
            BarrierDefinition.VisualStage stage = visual.stages().get(index);
            if (fraction <= stage.maximumDurabilityFraction()) {
                return new VisualSelection(stage.visual(), index + 1);
            }
        }
        return new VisualSelection(visual.id(), 0);
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

    public enum Removal {
        EXPLICIT,
        BROKEN,
        EXPIRED
    }

    private record VisualSelection(Identifier id, int stage) {
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
            this.priority = priority;
            this.expiresAt = expiresAt;
            this.position = position;
            this.syncedPosition = position;
            this.radius = radius;
            this.height = height;
        }
    }
}
