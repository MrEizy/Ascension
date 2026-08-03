package net.zic.ascension.impl.runtime.projectile;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.projectile.VirtualProjectileAccess;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VirtualProjectileInstance implements VirtualProjectileAccess {
    private final UUID runtimeId;
    private final ResourceKey<Level> dimension;
    private final UUID ownerId;
    private final Identifier skillId;
    private final Identifier definitionId;
    private final double charge;
    private final double maximumRange;
    private final Map<Identifier, Double> variables;
    private final Set<UUID> hitEntities = new HashSet<>();
    private Vec3 position;
    private Vec3 velocity;
    private UUID targetId;
    private double travelled;
    private int pierces;
    private int ticksLived;

    public VirtualProjectileInstance(
            ResourceKey<Level> dimension,
            UUID ownerId,
            Identifier skillId,
            Identifier definitionId,
            double charge,
            Vec3 position,
            Vec3 velocity,
            double maximumRange,
            UUID targetId,
            Map<Identifier, Double> variables
    ) {
        this.runtimeId = UUID.randomUUID();
        this.dimension = dimension;
        this.ownerId = ownerId;
        this.skillId = skillId;
        this.definitionId = definitionId;
        this.charge = charge;
        this.position = position;
        this.velocity = velocity;
        this.maximumRange = maximumRange;
        this.targetId = targetId;
        this.variables = variables == null ? Map.of() : Map.copyOf(variables);
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

    @Override
    public Identifier skillId() {
        return skillId;
    }

    public Identifier definitionId() {
        return definitionId;
    }

    public double charge() {
        return charge;
    }

    public double maximumRange() {
        return maximumRange;
    }

    public Map<Identifier, Double> variables() {
        return variables;
    }

    @Override
    public Vec3 position() {
        return position;
    }

    @Override
    public void setPosition(Vec3 position) {
        this.position = position;
    }

    @Override
    public Vec3 velocity() {
        return velocity;
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    @Override
    public UUID targetId() {
        return targetId;
    }

    @Override
    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    @Override
    public double travelled() {
        return travelled;
    }

    public void addTravelled(double value) {
        travelled += Math.max(0.0D, value);
    }

    @Override
    public int pierces() {
        return pierces;
    }

    public void addPierce() {
        pierces++;
    }

    public int ticksLived() {
        return ticksLived;
    }

    public void incrementTicksLived() {
        ticksLived++;
    }

    @Override
    public Set<UUID> hitEntities() {
        return hitEntities;
    }
}
