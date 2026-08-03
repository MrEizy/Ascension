package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AreaFieldInstance {
    private final UUID runtimeId;
    private final ResourceKey<Level> dimension;
    private final UUID ownerId;
    private final Identifier skillId;
    private final Identifier definitionId;
    private final Vec3 center;
    private final double charge;
    private final Map<Identifier, Double> variables;
    private final long createdAt;
    private final long expiresAt;
    private final Set<UUID> inside = new HashSet<>();

    public AreaFieldInstance(
            ResourceKey<Level> dimension,
            UUID ownerId,
            Identifier skillId,
            Identifier definitionId,
            Vec3 center,
            double charge,
            Map<Identifier, Double> variables,
            long createdAt,
            long expiresAt
    ) {
        this.runtimeId = UUID.randomUUID();
        this.dimension = dimension;
        this.ownerId = ownerId;
        this.skillId = skillId;
        this.definitionId = definitionId;
        this.center = center;
        this.charge = charge;
        this.variables = variables == null ? Map.of() : Map.copyOf(variables);
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID runtimeId() {
        return runtimeId;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public Identifier skillId() {
        return skillId;
    }

    public Identifier definitionId() {
        return definitionId;
    }

    public Vec3 center() {
        return center;
    }

    public double charge() {
        return charge;
    }

    public Map<Identifier, Double> variables() {
        return variables;
    }

    public long createdAt() {
        return createdAt;
    }

    public long expiresAt() {
        return expiresAt;
    }

    public Set<UUID> inside() {
        return inside;
    }
}
