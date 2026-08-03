package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;

import java.util.Map;
import java.util.UUID;

public final class OwnerBoundConstructInstance implements OwnerBoundConstructDefinition.View {
    private final UUID runtimeId;
    private final ResourceKey<Level> dimension;
    private final UUID ownerId;
    private final Identifier skillId;
    private final Identifier definitionId;
    private final double charge;
    private final Map<Identifier, Double> variables;
    private final double maximumStability;
    private final long expiresAt;
    private double stability;
    private Vec3 position;
    private Identifier visualId;
    private int visualStage;
    private double syncedStability;

    public OwnerBoundConstructInstance(
            ResourceKey<Level> dimension,
            UUID ownerId,
            Identifier skillId,
            Identifier definitionId,
            double charge,
            Map<Identifier, Double> variables,
            double stability,
            long expiresAt,
            Vec3 position
    ) {
        this.runtimeId = UUID.randomUUID();
        this.dimension = dimension;
        this.ownerId = ownerId;
        this.skillId = skillId;
        this.definitionId = definitionId;
        this.charge = charge;
        this.variables = variables == null ? Map.of() : Map.copyOf(variables);
        this.maximumStability = Math.max(0.0D, stability);
        this.stability = this.maximumStability;
        this.expiresAt = expiresAt;
        this.position = position;
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

    public double charge() {
        return charge;
    }

    public Map<Identifier, Double> variables() {
        return variables;
    }

    @Override
    public double stability() {
        return stability;
    }

    @Override
    public double maximumStability() {
        return maximumStability;
    }

    public double modifyStability(double amount) {
        double previous = stability;
        stability = Math.clamp(stability + amount, 0.0D, maximumStability);
        return stability - previous;
    }

    @Override
    public Vec3 position() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Identifier visualId() {
        return visualId;
    }

    public int visualStage() {
        return visualStage;
    }

    public void setVisualState(Identifier visualId, int visualStage) {
        this.visualId = visualId;
        this.visualStage = Math.max(0, visualStage);
        this.syncedStability = stability;
    }

    public double syncedStability() {
        return syncedStability;
    }

    public void markStabilitySynced() {
        syncedStability = stability;
    }

    @Override
    public long expiresAt() {
        return expiresAt;
    }
}
