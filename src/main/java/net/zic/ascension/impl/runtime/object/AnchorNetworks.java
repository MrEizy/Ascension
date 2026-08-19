package net.zic.ascension.impl.runtime.object;

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
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.runtime.AnchorNetworkDefinition;


import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;

import java.util.*;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AnchorNetworks {
    private static final List<Instance> NETWORKS = new ArrayList<>();

    private AnchorNetworks() {
    }

    public static UUID spawn(
            SkillActionContext context,
            Identifier definitionId,
            Vec3 center
    ) {
        AnchorNetworkDefinition definition = SkillDefinitions.resolveStored(
                AnchorNetworkDefinition.class,
                context.skill(),
                definitionId,
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
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
        Instance network = new Instance(
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

        Resolved<AreaFieldDefinition> field = definition.field()
                .map(reference -> SkillDefinitions.field(context, reference))
                .orElse(null);
        if (field != null) {
            UUID child = AreaFields.spawn(context, field.id(), resolvedCenter);
            network.setChildField(child);
        }
        Resolved<RuntimeVisualDefinition> visual = visual(context, definition.visual());
        if (visual != null) {
            RuntimeVisualSync.spawn(
                    context.level(),
                    visualState(network, definition, visual.id(), network.expiresAt())
            );
        }
        return network.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        if (runtimeId == null) {
            return false;
        }
        Iterator<Instance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            Instance network = iterator.next();
            if (!network.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeChildField(null, network, false);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<Instance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            Instance network = iterator.next();
            if (!network.runtimeId().equals(runtimeId)) {
                continue;
            }
            removeRuntime(level, network, false);
            iterator.remove();
            return true;
        }
        return false;
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int removed = 0;
        Iterator<Instance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            Instance network = iterator.next();
            if (!network.ownerId().equals(ownerId)
                    || definitionId != null && !network.definitionId().equals(definitionId)) {
                continue;
            }
            removeChildField(null, network, false);
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
        Iterator<Instance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            Instance network = iterator.next();
            if (!network.ownerId().equals(ownerId)
                    || definitionId != null && !network.definitionId().equals(definitionId)) {
                continue;
            }
            removeRuntime(level, network, false);
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
        Iterator<Instance> iterator = NETWORKS.iterator();
        while (iterator.hasNext()) {
            Instance network = iterator.next();
            ServerLevel level = event.getServer().getLevel(network.dimension());
            if (level == null || !tick(level, network)) {
                if (level != null) {
                    removeRuntime(level, network, level.getGameTime() >= network.expiresAt());
                } else {
                    removeChildField(null, network, false);
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NETWORKS.clear();
    }

    private static boolean tick(ServerLevel level, Instance network) {
        Entity entity = level.getEntity(network.ownerId());
        if (!(entity instanceof ServerPlayer) || entity.isRemoved()) {
            return false;
        }
        AnchorNetworkDefinition definition = SkillDefinitions.resolveStored(
                AnchorNetworkDefinition.class,
                network.skillId(),
                network.definitionId(),
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
                level.registryAccess()
        );
        if (definition == null) {
            return false;
        }
        return level.getGameTime() < network.expiresAt();
    }

    private static RuntimeVisualState visualState(
            Instance network,
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

    private static Resolved<RuntimeVisualDefinition> visual(
            SkillActionContext context,
            Optional<Identifier> reference
    ) {
        return reference.map(value -> SkillDefinitions.visual(context, value)).orElse(null);
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

    private static void removeRuntime(ServerLevel level, Instance network, boolean expired) {
        AnchorNetworkDefinition definition = SkillDefinitions.resolveStored(
                AnchorNetworkDefinition.class,
                network.skillId(),
                network.definitionId(),
                CoreRegistries.ANCHOR_NETWORK_REGISTRY,
                level.registryAccess()
        );
        Entity ownerEntity = level.getEntity(network.ownerId());
        if (definition != null && ownerEntity instanceof ServerPlayer owner) {
            SkillActionContext context = new SkillActionContext(
                    level,
                    owner,
                    network.skillId(),
                    null,
                    network.center(),
                    network.charge(),
                    network.variables()
            );
            Resolved<RuntimeVisualDefinition> visual = visual(context, definition.visual());
            if (visual != null) {
                RuntimeVisualSync.remove(
                        level,
                        visualState(network, definition, visual.id(), 0L)
                );
            }
        }
        removeChildField(level, network, expired);
    }

    private static void removeChildField(ServerLevel level, Instance network, boolean expired) {
        if (network.childField() == null) {
            return;
        }
        if (level == null) {
            AreaFields.remove(network.childField());
        } else if (expired) {
            AreaFields.expire(level, network.childField());
        } else {
            AreaFields.remove(level, network.childField());
        }
    }

    private static final class Instance implements AnchorNetworkDefinition.View {
        private final UUID runtimeId = UUID.randomUUID();
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Identifier skillId;
        private final Identifier definitionId;
        private final Vec3 center;
        private final Map<Identifier, Vec3> nodes;
        private final double charge;
        private final Map<Identifier, Double> variables;
        private final long expiresAt;
        private UUID childField;

        private Instance(
                ResourceKey<Level> dimension,
                UUID ownerId,
                Identifier skillId,
                Identifier definitionId,
                Vec3 center,
                Map<Identifier, Vec3> nodes,
                double charge,
                Map<Identifier, Double> variables,
                long expiresAt
        ) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.skillId = skillId;
            this.definitionId = definitionId;
            this.center = center;
            this.nodes = Map.copyOf(nodes);
            this.charge = charge;
            this.variables = variables == null ? Map.of() : Map.copyOf(variables);
            this.expiresAt = expiresAt;
        }

        @Override
        public UUID runtimeId() {
            return runtimeId;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        @Override
        public UUID ownerId() {
            return ownerId;
        }

        public Identifier skillId() {
            return skillId;
        }

        @Override
        public Identifier definitionId() {
            return definitionId;
        }

        @Override
        public Vec3 center() {
            return center;
        }

        @Override
        public Map<Identifier, Vec3> nodes() {
            return nodes;
        }

        public double charge() {
            return charge;
        }

        public Map<Identifier, Double> variables() {
            return variables;
        }

        @Override
        public long expiresAt() {
            return expiresAt;
        }

        public UUID childField() {
            return childField;
        }

        public void setChildField(UUID childField) {
            this.childField = childField;
        }
    }
}
