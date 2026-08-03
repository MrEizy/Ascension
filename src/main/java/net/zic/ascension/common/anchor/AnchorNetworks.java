package net.zic.ascension.common.anchor;

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
import net.zic.ascension.api.core.anchor.AnchorNetworkDefinition;


import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.common.field.AreaFields;
import net.zic.ascension.impl.core.anchor.AnchorNetworkInstance;
import net.zic.ascension.common.visual.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AnchorNetworks {
    private static final List<AnchorNetworkInstance> NETWORKS = new ArrayList<>();

    private AnchorNetworks() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId,
            Vec3 center
    ) {
        AnchorNetworkDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
                definitionId,
                context.level().registryAccess()
        );
        if (definition == null || definition.nodes().isEmpty()) {
            return null;
        }

        double durationValue = definition.duration().resolve(context.scaledValueContext());
        if (!Double.isFinite(durationValue) || durationValue <= 0.0D) {
            return null;
        }

        Vec3 resolvedCenter = center == null ? context.position() : center;
        Map<Identifier, Vec3> nodes = resolveNodes(
                resolvedCenter,
                context.caster().getYRot(),
                definition
        );
        AnchorNetworkInstance network = new AnchorNetworkInstance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                resolvedCenter,
                nodes,
                context.charge(),
                context.variables(),
                context.level().getGameTime() + Math.max(1L, Math.round(durationValue))
        );
        NETWORKS.add(network);

        if (definition.field().isPresent()) {
            UUID field = AreaFields.spawn(context, definition.field().get(), resolvedCenter);
            network.setChildField(field);
        }
        definition.visual().ifPresent(visual -> RuntimeVisualSync.spawn(
                context.level(),
                visualState(network, definition, visual, network.expiresAt())
        ));
        return network.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        if (runtimeId == null) {
            return false;
        }
        Iterator<AnchorNetworkInstance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            AnchorNetworkInstance network = iterator.next();
            if (!network.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeChildField(null, network);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<AnchorNetworkInstance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            AnchorNetworkInstance network = iterator.next();
            if (!network.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeRuntime(level, network);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int removed = 0;
        Iterator<AnchorNetworkInstance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            AnchorNetworkInstance network = iterator.next();
            if (!network.ownerId().equals(ownerId)
                    || definitionId != null && !network.definitionId().equals(definitionId)) {
                continue;
            }
            removeChildField(null, network);
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
        Iterator<AnchorNetworkInstance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            AnchorNetworkInstance network = iterator.next();
            if (!network.ownerId().equals(ownerId)
                    || definitionId != null && !network.definitionId().equals(definitionId)) {
                continue;
            }
            removeRuntime(level, network);
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public static List<AnchorNetworkDefinition.View> findOwned(UUID ownerId, Identifier definitionId) {
        return NETWORKS.stream()
                .filter(value -> value.ownerId().equals(ownerId))
                .filter(value -> definitionId == null || value.definitionId().equals(definitionId))
                .map(value -> (AnchorNetworkDefinition.View) value)
                .toList();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<AnchorNetworkInstance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            AnchorNetworkInstance network = iterator.next();
            ServerLevel level = event.getServer().getLevel(network.dimension());
            if (level == null || !tick(level, network)) {
                if (level != null) {
                    removeRuntime(level, network);
                } else {
                    removeChildField(null, network);
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NETWORKS.clear();
    }

    private static boolean tick(ServerLevel level, AnchorNetworkInstance network) {
        Entity entity = level.getEntity(network.ownerId());
        if (!(entity instanceof ServerPlayer) || entity.isRemoved()) {
            return false;
        }
        AnchorNetworkDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
                network.definitionId(),
                level.registryAccess()
        );
        if (definition == null) {
            return false;
        }
        return level.getGameTime() < network.expiresAt();
    }

    private static RuntimeVisualState visualState(
            AnchorNetworkInstance network,
            AnchorNetworkDefinition definition,
            Identifier visual,
            long expiresAt
    ) {
        List<Vec3> points = new ArrayList<>();
        Map<Identifier, Integer> indices = new HashMap<>();
        for (AnchorNetworkDefinition.Node anchor : definition.nodes()) {
            Vec3 position = network.nodes().get(anchor.id());
            if (position == null) {
                continue;
            }
            indices.put(anchor.id(), points.size());
            points.add(position);
        }

        List<RuntimeVisualState.Link> links = new ArrayList<>();
        definition.links().forEach(link -> {
            Integer from = indices.get(link.from());
            Integer to = indices.get(link.to());
            if (from != null && to != null) {
                links.add(new RuntimeVisualState.Link(from, to));
            }
        });

        return new RuntimeVisualState(
                network.runtimeId(),
                visual,
                network.ownerId(),
                network.center(),
                Vec3.ZERO,
                points,
                links,
                expiresAt,
                0,
                0,
                0.0F,
                network.runtimeId().getMostSignificantBits(),
                points.size(),
                links.size()
        );
    }

    private static Map<Identifier, Vec3> resolveNodes(
            Vec3 center,
            float casterYaw,
            AnchorNetworkDefinition definition
    ) {
        Map<Identifier, Vec3> nodes = new HashMap<>();
        double radians = Math.toRadians(-casterYaw);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        for (AnchorNetworkDefinition.Node anchor : definition.nodes()) {
            Vec3 offset = anchor.offset();
            if (definition.rotateWithCaster()) {
                offset = new Vec3(
                        offset.x * cos - offset.z * sin,
                        offset.y,
                        offset.x * sin + offset.z * cos
                );
            }
            nodes.put(anchor.id(), center.add(offset));
        }
        return Map.copyOf(nodes);
    }

    private static void removeRuntime(ServerLevel level, AnchorNetworkInstance network) {
        AnchorNetworkDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
                network.definitionId(),
                level.registryAccess()
        );
        if (definition != null) {
            definition.visual().ifPresent(visual -> RuntimeVisualSync.remove(
                    level,
                    visualState(network, definition, visual, 0L)
            ));
        }
        removeChildField(level, network);
    }

    private static void removeChildField(ServerLevel level, AnchorNetworkInstance network) {
        if (network.childField() == null) {
            return;
        }
        if (level == null) {
            AreaFields.remove(network.childField());
        } else {
            AreaFields.remove(level, network.childField());
        }
    }
}
