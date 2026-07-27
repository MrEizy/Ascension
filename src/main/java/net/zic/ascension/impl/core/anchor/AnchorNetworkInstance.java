package net.zic.ascension.impl.core.anchor;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.anchor.AnchorNetworkDefinition;

import java.util.Map;
import java.util.UUID;

public final class AnchorNetworkInstance implements AnchorNetworkDefinition.View {
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

    public AnchorNetworkInstance(
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
