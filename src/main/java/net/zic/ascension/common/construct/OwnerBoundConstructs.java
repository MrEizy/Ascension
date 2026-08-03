package net.zic.ascension.common.construct;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.client.visual.RuntimeVisualState;

import net.zic.ascension.api.core.construct.OwnerBoundConstructDefinition;

import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.common.visual.RuntimeVisualSync;
import net.zic.ascension.impl.core.construct.OwnerBoundConstructInstance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class OwnerBoundConstructs {
    private static final List<OwnerBoundConstructInstance> CONSTRUCTS = new ArrayList<>();

    private OwnerBoundConstructs() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId
    ) {
        OwnerBoundConstructDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.CONSTRUCT_REGISTRY,
                definitionId,
                context.level().registryAccess()
        );
        if (definition == null) {
            return null;
        }

        double duration = definition.duration().resolve(context.scaledValueContext());
        double stability = definition.stability().resolve(context.scaledValueContext());
        if (!Double.isFinite(duration)
                || duration <= 0.0D
                || !Double.isFinite(stability)
                || stability <= 0.0D) {
            return null;
        }

        OwnerBoundConstructInstance construct = new OwnerBoundConstructInstance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                context.charge(),
                context.variables(),
                stability,
                context.level().getGameTime() + Math.max(1L, Math.round(duration)),
                resolvePosition(context.caster(), definition)
        );
        CONSTRUCTS.add(construct);
        VisualSelection visual = resolveVisual(definition, construct);
        if (visual.visual() != null) {
            construct.setVisualState(visual.visual(), visual.stage());
            RuntimeVisualSync.spawn(
                    context.level(),
                    visualState(construct, definition, visual.visual(), visual.stage(), construct.expiresAt())
            );
        }
        return construct.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        return runtimeId != null && CONSTRUCTS.removeIf(value -> value.runtimeId().equals(runtimeId));
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<OwnerBoundConstructInstance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            OwnerBoundConstructInstance construct = iterator.next();
            if (!construct.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeRuntime(level, construct);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int before = CONSTRUCTS.size();
        CONSTRUCTS.removeIf(value -> value.ownerId().equals(ownerId)
                && (definitionId == null || value.definitionId().equals(definitionId)));
        return before - CONSTRUCTS.size();
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        if (level == null || ownerId == null) {
            return 0;
        }
        int removed = 0;
        Iterator<OwnerBoundConstructInstance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            OwnerBoundConstructInstance construct = iterator.next();
            if (!construct.ownerId().equals(ownerId)
                    || definitionId != null && !construct.definitionId().equals(definitionId)) {
                continue;
            }
            removeRuntime(level, construct);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static double modifyStability(UUID runtimeId, double amount) {
        if (runtimeId == null || !Double.isFinite(amount)) {
            return 0.0D;
        }
        for (OwnerBoundConstructInstance construct : CONSTRUCTS) {
            if (construct.runtimeId().equals(runtimeId)) {
                return construct.modifyStability(amount);
            }
        }
        return 0.0D;
    }

    public static List<OwnerBoundConstructDefinition.View> findOwned(UUID ownerId, Identifier definitionId) {
        return CONSTRUCTS.stream()
                .filter(value -> value.ownerId().equals(ownerId))
                .filter(value -> definitionId == null || value.definitionId().equals(definitionId))
                .map(value -> (OwnerBoundConstructDefinition.View) value)
                .toList();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<OwnerBoundConstructInstance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            OwnerBoundConstructInstance construct = iterator.next();
            ServerLevel level = event.getServer().getLevel(construct.dimension());
            if (level == null || !tick(level, construct)) {
                if (level != null) {
                    removeRuntime(level, construct);
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CONSTRUCTS.clear();
    }

    private static boolean tick(
            ServerLevel level,
            OwnerBoundConstructInstance construct
    ) {
        Entity entity = level.getEntity(construct.ownerId());
        if (!(entity instanceof ServerPlayer owner) || owner.isRemoved()) {
            return false;
        }

        OwnerBoundConstructDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.CONSTRUCT_REGISTRY,
                construct.definitionId(),
                level.registryAccess()
        );
        if (definition == null) {
            return false;
        }
        if (construct.stability() <= 0.0D || level.getGameTime() >= construct.expiresAt()) {
            return false;
        }

        construct.setPosition(resolvePosition(owner, definition));
        VisualSelection visual = resolveVisual(definition, construct);
        if (visual.visual() == null && construct.visualId() != null) {
            RuntimeVisualSync.remove(
                    level,
                    visualState(
                            construct,
                            definition,
                            construct.visualId(),
                            construct.visualStage(),
                            0L
                    )
            );
            construct.setVisualState(null, 0);
        } else if (visual.visual() != null) {
            boolean visualChanged = !visual.visual().equals(construct.visualId())
                    || visual.stage() != construct.visualStage();
            boolean stabilityChanged = Math.abs(construct.stability() - construct.syncedStability())
                    >= Math.max(1.0D, construct.maximumStability() * 0.05D);
            if (visualChanged || stabilityChanged) {
                construct.setVisualState(visual.visual(), visual.stage());
                RuntimeVisualSync.update(
                        level,
                        visualState(construct, definition, visual.visual(), visual.stage(), construct.expiresAt())
                );
                construct.markStabilitySynced();
            }
        }
        return true;
    }

    private static void removeRuntime(
            ServerLevel level,
            OwnerBoundConstructInstance construct
    ) {
        if (construct.visualId() == null) {
            return;
        }
        OwnerBoundConstructDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.CONSTRUCT_REGISTRY,
                construct.definitionId(),
                level.registryAccess()
        );
        if (definition != null) {
            RuntimeVisualSync.remove(
                    level,
                    visualState(
                            construct,
                            definition,
                            construct.visualId(),
                            construct.visualStage(),
                            0L
                    )
            );
        }
    }

    private static VisualSelection resolveVisual(
            OwnerBoundConstructDefinition definition,
            OwnerBoundConstructInstance construct
    ) {
        double fraction = construct.maximumStability() <= 0.0D
                ? 0.0D
                : construct.stability() / construct.maximumStability();
        for (int index = 0; index < definition.visualStages().size(); index++) {
            OwnerBoundConstructDefinition.VisualStage stage = definition.visualStages().get(index);
            if (fraction <= stage.maximumStabilityFraction()) {
                return new VisualSelection(stage.visual(), index + 1);
            }
        }
        return new VisualSelection(definition.visual().orElse(null), 0);
    }

    private static RuntimeVisualState visualState(
            OwnerBoundConstructInstance construct,
            OwnerBoundConstructDefinition definition,
            Identifier visual,
            int stage,
            long expiresAt
    ) {
        float progress = construct.maximumStability() <= 0.0D
                ? 0.0F
                : (float) Math.clamp(
                        construct.stability() / construct.maximumStability(),
                        0.0D,
                        1.0D
                );
        return new RuntimeVisualState(
                construct.runtimeId(),
                visual,
                construct.ownerId(),
                construct.position(),
                definition.offset(),
                List.of(),
                List.of(),
                expiresAt,
                stage,
                RuntimeVisualState.OWNER_RELATIVE
                        | (definition.rotateWithOwner() ? RuntimeVisualState.ROTATE_WITH_OWNER : 0),
                progress,
                construct.runtimeId().getMostSignificantBits(),
                construct.stability(),
                construct.maximumStability()
        );
    }

    private static Vec3 resolvePosition(
            ServerPlayer owner,
            OwnerBoundConstructDefinition definition
    ) {
        Vec3 offset = definition.offset();
        if (definition.rotateWithOwner()) {
            double radians = Math.toRadians(-owner.getYRot());
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            offset = new Vec3(
                    offset.x * cos - offset.z * sin,
                    offset.y,
                    offset.x * sin + offset.z * cos
            );
        }
        return owner.position().add(offset);
    }
    private record VisualSelection(Identifier visual, int stage) {
    }
}
