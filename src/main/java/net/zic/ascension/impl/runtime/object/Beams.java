package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.runtime.BeamDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.impl.core.skill.castable.SkillActionRuntime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class Beams {
    public static final Identifier DISTANCE = AscensionCraft.prefix("beam/distance");
    public static final Identifier RANGE_FRACTION = AscensionCraft.prefix("beam/range_fraction");
    private static final List<Instance> ACTIVE = new ArrayList<>();

    private Beams() {
    }

    public static void spawn(SkillActionContext context, SkillDefinitions.Resolved<BeamDefinition> resolved) {
        if (context == null || resolved == null || resolved.value() == null) {
            return;
        }
        BeamDefinition definition = resolved.value();
        int duration = Math.max(1, (int) Math.round(definition.duration().resolve(context.scaledValueContext())));
        Instance instance = new Instance(UUID.randomUUID(), context, definition, context.level().getGameTime() + duration);
        ACTIVE.add(instance);
        tick(instance, true);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<Instance> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            long gameTime = instance.context().level().getGameTime();
            if (gameTime >= instance.expiresAt() || instance.context().caster().isRemoved() || !instance.context().caster().isAlive()) {
                finish(instance);
                iterator.remove();
                continue;
            }
            if ((gameTime - instance.startedAt()) % instance.definition().tickInterval() == 0L) {
                tick(instance, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private static void tick(Instance instance, boolean spawnVisual) {
        SkillActionContext context = instance.context();
        LivingEntity caster = context.caster();
        ServerLevel level = context.level();
        BeamDefinition definition = instance.definition();
        double range = Math.max(0.0D, definition.range().resolve(context.scaledValueContext()));
        double width = Math.max(0.01D, definition.width().resolve(context.scaledValueContext()));
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().normalize().scale(range));
        HitResult blockHit = null;
        if (definition.stopOnBlock()) {
            blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if (blockHit.getType() != HitResult.Type.MISS) {
                end = blockHit.getLocation();
            }
        }
        updateVisual(instance, start, end, width, spawnVisual);
        Vec3 delta = end.subtract(start);
        double lengthSqr = delta.lengthSqr();
        AABB bounds = new AABB(
                Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z),
                Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z)
        ).inflate(width);
        List<Hit> hits = new ArrayList<>();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> entity != caster && definition.filter().matches(caster, entity))) {
            Vec3 center = target.getBoundingBox().getCenter();
            double t = lengthSqr <= 1.0E-8D ? 0.0D : Math.clamp(center.subtract(start).dot(delta) / lengthSqr, 0.0D, 1.0D);
            Vec3 closest = start.add(delta.scale(t));
            double radius = width + Math.max(target.getBbWidth(), target.getBbHeight()) * 0.25D;
            if (center.distanceToSqr(closest) <= radius * radius) {
                hits.add(new Hit(target, t, closest));
            }
        }
        hits.sort(Comparator.comparingDouble(Hit::fraction));
        int limit = definition.maximumTargets() <= 0 ? hits.size() : Math.min(definition.maximumTargets(), hits.size());
        for (int i = 0; i < limit; i++) {
            Hit hit = hits.get(i);
            Map<Identifier, Double> variables = new HashMap<>(context.variables());
            double distance = Math.sqrt(lengthSqr) * hit.fraction();
            variables.put(DISTANCE, distance);
            variables.put(RANGE_FRACTION, range <= 0.0D ? 0.0D : distance / range);
            SkillActionContext hitContext = new SkillActionContext(
                    level, caster, context.skill(), hit.entity(), hit.position(), context.charge(), variables, context.attribution()
            );
            double knockback = definition.knockback().resolve(hitContext.scaledValueContext());
            if (knockback != 0.0D) {
                Vec3 direction = delta.normalize().scale(knockback);
                hit.entity().push(direction.x, Math.max(0.0D, direction.y), direction.z);
            }
            SkillActionRuntime.execute(hitContext, definition.onHit());
        }
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS && !definition.onBlock().isEmpty()) {
            SkillActionRuntime.execute(new SkillActionContext(
                    level, caster, context.skill(), null, end, context.charge(), context.variables(), context.attribution()
            ), definition.onBlock());
        }
    }

    private static void updateVisual(Instance instance, Vec3 start, Vec3 end, double width, boolean spawn) {
        if (instance.definition().visual().isEmpty()) {
            return;
        }
        RuntimeVisualState state = new RuntimeVisualState(
                instance.runtimeId(), instance.definition().visual().get(), instance.context().caster().getUUID(), start, Vec3.ZERO,
                List.of(start, end), List.of(new RuntimeVisualState.Link(0, 1)), instance.expiresAt(), 0, 0, 0.0F,
                instance.runtimeId().getMostSignificantBits(), width, start.distanceTo(end), (float) width, 0.0D,
                RuntimeVisualState.WHITE_TINT, RuntimeVisualState.WHITE_TINT, null
        );
        if (spawn) {
            RuntimeVisualSync.spawn(instance.context().level(), state);
        } else {
            RuntimeVisualSync.update(instance.context().level(), state);
        }
        instance.visualState = state;
    }

    private static void finish(Instance instance) {
        if (instance.visualState != null) {
            RuntimeVisualSync.remove(instance.context().level(), instance.visualState);
        }
        SkillActionRuntime.execute(instance.context(), instance.definition().onExpire());
    }

    private static final class Instance {
        private final UUID runtimeId;
        private final SkillActionContext context;
        private final BeamDefinition definition;
        private final long startedAt;
        private final long expiresAt;
        private RuntimeVisualState visualState;

        private Instance(UUID runtimeId, SkillActionContext context, BeamDefinition definition, long expiresAt) {
            this.runtimeId = runtimeId;
            this.context = context;
            this.definition = definition;
            this.startedAt = context.level().getGameTime();
            this.expiresAt = expiresAt;
        }
        UUID runtimeId() { return runtimeId; }
        SkillActionContext context() { return context; }
        BeamDefinition definition() { return definition; }
        long startedAt() { return startedAt; }
        long expiresAt() { return expiresAt; }
    }

    private record Hit(LivingEntity entity, double fraction, Vec3 position) {
    }
}
