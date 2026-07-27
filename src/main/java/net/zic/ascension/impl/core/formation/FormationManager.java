package net.zic.ascension.impl.core.formation;

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
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.client.visual.RuntimeVisualKind;
import net.zic.ascension.api.client.visual.RuntimeVisualLink;
import net.zic.ascension.api.client.visual.RuntimeVisualState;
import net.zic.ascension.api.core.formation.FormationAnchorDefinition;
import net.zic.ascension.api.core.formation.FormationDefinition;
import net.zic.ascension.api.core.formation.FormationInstanceView;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.common.field.AreaFieldService;
import net.zic.ascension.common.visual.RuntimeVisualSyncManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class FormationManager {
    private static final List<FormationInstance> FORMATIONS = new ArrayList<>();

    private FormationManager() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId,
            Vec3 center
    ) {
        FormationDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.FORMATION_REGISTRY,
                definitionId,
                context.level().registryAccess()
        );
        if (definition == null || definition.anchors().isEmpty()) {
            return null;
        }

        double durationValue = definition.duration().resolve(context.scaledValueContext());
        if (!Double.isFinite(durationValue) || durationValue <= 0.0D) {
            return null;
        }

        Vec3 resolvedCenter = center == null ? context.position() : center;
        Map<Identifier, Vec3> anchors = resolveAnchors(
                resolvedCenter,
                context.caster().getYRot(),
                definition
        );
        FormationInstance formation = new FormationInstance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                resolvedCenter,
                anchors,
                context.charge(),
                context.variables(),
                context.level().getGameTime() + Math.max(1L, Math.round(durationValue))
        );
        FORMATIONS.add(formation);

        if (definition.field().isPresent()) {
            UUID field = AreaFieldService.spawn(context, definition.field().get(), resolvedCenter);
            formation.setChildField(field);
        }
        definition.visual().ifPresent(visual -> RuntimeVisualSyncManager.spawn(
                context.level(),
                visualState(formation, definition, visual, formation.expiresAt())
        ));
        return formation.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        if (runtimeId == null) {
            return false;
        }
        Iterator<FormationInstance> iterator = FORMATIONS.iterator();
        while (iterator.hasNext()) {
            FormationInstance formation = iterator.next();
            if (!formation.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeChildField(null, formation);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<FormationInstance> iterator = FORMATIONS.iterator();
        while (iterator.hasNext()) {
            FormationInstance formation = iterator.next();
            if (!formation.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeRuntime(level, formation);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int removed = 0;
        Iterator<FormationInstance> iterator = FORMATIONS.iterator();
        while (iterator.hasNext()) {
            FormationInstance formation = iterator.next();
            if (!formation.ownerId().equals(ownerId)
                    || definitionId != null && !formation.definitionId().equals(definitionId)) {
                continue;
            }
            removeChildField(null, formation);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        if (level == null || ownerId == null) {
            return 0;
        }
        int removed = 0;
        Iterator<FormationInstance> iterator = FORMATIONS.iterator();
        while (iterator.hasNext()) {
            FormationInstance formation = iterator.next();
            if (!formation.ownerId().equals(ownerId)
                    || definitionId != null && !formation.definitionId().equals(definitionId)) {
                continue;
            }
            removeRuntime(level, formation);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static List<FormationInstanceView> findOwned(UUID ownerId, Identifier definitionId) {
        return FORMATIONS.stream()
                .filter(value -> value.ownerId().equals(ownerId))
                .filter(value -> definitionId == null || value.definitionId().equals(definitionId))
                .map(value -> (FormationInstanceView) value)
                .toList();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<FormationInstance> iterator = FORMATIONS.iterator();
        while (iterator.hasNext()) {
            FormationInstance formation = iterator.next();
            ServerLevel level = event.getServer().getLevel(formation.dimension());
            if (level == null || !tick(level, formation)) {
                if (level != null) {
                    removeRuntime(level, formation);
                } else {
                    removeChildField(null, formation);
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        FORMATIONS.clear();
    }

    private static boolean tick(ServerLevel level, FormationInstance formation) {
        Entity entity = level.getEntity(formation.ownerId());
        if (!(entity instanceof ServerPlayer) || entity.isRemoved()) {
            return false;
        }
        FormationDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.FORMATION_REGISTRY,
                formation.definitionId(),
                level.registryAccess()
        );
        if (definition == null) {
            return false;
        }
        return level.getGameTime() < formation.expiresAt();
    }

    private static RuntimeVisualState visualState(
            FormationInstance formation,
            FormationDefinition definition,
            Identifier visual,
            long expiresAt
    ) {
        List<Vec3> points = new ArrayList<>();
        Map<Identifier, Integer> indices = new HashMap<>();
        for (FormationAnchorDefinition anchor : definition.anchors()) {
            Vec3 position = formation.anchors().get(anchor.id());
            if (position == null) {
                continue;
            }
            indices.put(anchor.id(), points.size());
            points.add(position);
        }

        List<RuntimeVisualLink> links = new ArrayList<>();
        definition.links().forEach(link -> {
            Integer from = indices.get(link.from());
            Integer to = indices.get(link.to());
            if (from != null && to != null) {
                links.add(new RuntimeVisualLink(from, to));
            }
        });

        return new RuntimeVisualState(
                formation.runtimeId(),
                RuntimeVisualKind.FORMATION,
                visual,
                formation.ownerId(),
                formation.center(),
                Vec3.ZERO,
                points,
                links,
                expiresAt,
                0,
                0,
                0.0F,
                formation.runtimeId().getMostSignificantBits(),
                points.size(),
                links.size()
        );
    }

    private static Map<Identifier, Vec3> resolveAnchors(
            Vec3 center,
            float casterYaw,
            FormationDefinition definition
    ) {
        Map<Identifier, Vec3> anchors = new HashMap<>();
        double radians = Math.toRadians(-casterYaw);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        for (FormationAnchorDefinition anchor : definition.anchors()) {
            Vec3 offset = anchor.offset();
            if (definition.rotateWithCaster()) {
                offset = new Vec3(
                        offset.x * cos - offset.z * sin,
                        offset.y,
                        offset.x * sin + offset.z * cos
                );
            }
            anchors.put(anchor.id(), center.add(offset));
        }
        return Map.copyOf(anchors);
    }

    private static void removeRuntime(ServerLevel level, FormationInstance formation) {
        FormationDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.FORMATION_REGISTRY,
                formation.definitionId(),
                level.registryAccess()
        );
        if (definition != null) {
            definition.visual().ifPresent(visual -> RuntimeVisualSyncManager.remove(
                    level,
                    visualState(formation, definition, visual, 0L)
            ));
        }
        removeChildField(level, formation);
    }

    private static void removeChildField(ServerLevel level, FormationInstance formation) {
        if (formation.childField() == null) {
            return;
        }
        if (level == null) {
            AreaFieldService.remove(formation.childField());
        } else {
            AreaFieldService.remove(level, formation.childField());
        }
    }
}
