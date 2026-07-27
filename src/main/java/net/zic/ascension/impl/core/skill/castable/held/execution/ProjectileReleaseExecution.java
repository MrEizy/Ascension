package net.zic.ascension.impl.core.skill.castable.held.execution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.core.projectile.VirtualProjectileManager;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastExecutionTypes;

import java.util.List;
import java.util.Optional;

public record ProjectileReleaseExecution(
        ScaledValue speed,
        ScaledValue range,
        double gravity,
        double hitRadius,
        int pierces,
        TargetFilterDefinition filter,
        Optional<Identifier> flightParticle,
        List<ProjectileBehavior> behaviors,
        List<SkillExecutionFeature> features,
        Optional<Identifier> visual
) implements HeldCastExecution {
    public static final MapCodec<ProjectileReleaseExecution> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("speed").forGetter(ProjectileReleaseExecution::speed),
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(ProjectileReleaseExecution::range),
            Codec.DOUBLE.optionalFieldOf("gravity", 0.0D).forGetter(ProjectileReleaseExecution::gravity),
            Codec.DOUBLE.optionalFieldOf("hit_radius", 0.3D).forGetter(ProjectileReleaseExecution::hitRadius),
            Codec.intRange(0, 64).optionalFieldOf("pierces", 0).forGetter(ProjectileReleaseExecution::pierces),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(ProjectileReleaseExecution::filter),
            Identifier.CODEC.optionalFieldOf("flight_particle").forGetter(ProjectileReleaseExecution::flightParticle),
            ProjectileBehavior.CODEC.listOf().optionalFieldOf("behaviors", List.of())
                    .forGetter(ProjectileReleaseExecution::behaviors),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features", List.of())
                    .forGetter(ProjectileReleaseExecution::features),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(ProjectileReleaseExecution::visual)
    ).apply(instance, ProjectileReleaseExecution::new));

    public ProjectileReleaseExecution {
        gravity = Double.isFinite(gravity) ? Math.clamp(gravity, -4.0D, 4.0D) : 0.0D;
        hitRadius = Double.isFinite(hitRadius) ? Math.clamp(hitRadius, 0.0D, 4.0D) : 0.3D;
        filter = filter == null ? TargetFilterDefinition.hostile() : filter;
        flightParticle = flightParticle == null ? Optional.empty() : flightParticle;
        behaviors = behaviors == null ? List.of() : List.copyOf(behaviors);
        features = features == null ? List.of() : List.copyOf(features);
        visual = visual == null ? Optional.empty() : visual;
    }

    @Override
    public HeldCastExecutionType getType() {
        return AscensionHeldCastExecutionTypes.PROJECTILE_RELEASE.get();
    }

    @Override
    public void execute(HeldCastExecutionContext context) {
        VirtualProjectileManager.spawn(context, this);
    }
}
