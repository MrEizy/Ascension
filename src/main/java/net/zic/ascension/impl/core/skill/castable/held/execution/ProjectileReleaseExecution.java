package net.zic.ascension.impl.core.skill.castable.held.execution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.core.skill.castable.held.HeldCastProjectileManager;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastExecutionTypes;

import java.util.List;
import java.util.Optional;

public record ProjectileReleaseExecution(
        ScaledValue speed,
        ScaledValue range,
        double gravity,
        double hitRadius,
        int pierces,
        Optional<Identifier> flightParticle,
        List<HeldCastReleaseFeature> features
) implements HeldCastExecution {
    public static final MapCodec<ProjectileReleaseExecution> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("speed").forGetter(ProjectileReleaseExecution::speed),
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(ProjectileReleaseExecution::range),
            Codec.DOUBLE.optionalFieldOf("gravity", 0.0D).forGetter(ProjectileReleaseExecution::gravity),
            Codec.DOUBLE.optionalFieldOf("hit_radius", 0.3D).forGetter(ProjectileReleaseExecution::hitRadius),
            Codec.intRange(0, 64).optionalFieldOf("pierces", 0).forGetter(ProjectileReleaseExecution::pierces),
            Identifier.CODEC.optionalFieldOf("flight_particle").forGetter(ProjectileReleaseExecution::flightParticle),
            HeldCastReleaseFeature.CODEC.listOf().optionalFieldOf("features", List.of())
                    .forGetter(ProjectileReleaseExecution::features)
    ).apply(instance, ProjectileReleaseExecution::new));

    public ProjectileReleaseExecution {
        gravity = Double.isFinite(gravity) ? Math.clamp(gravity, -4.0D, 4.0D) : 0.0D;
        hitRadius = Double.isFinite(hitRadius) ? Math.clamp(hitRadius, 0.0D, 4.0D) : 0.3D;
        flightParticle = flightParticle == null ? Optional.empty() : flightParticle;
        features = features == null ? List.of() : List.copyOf(features);
    }

    @Override
    public HeldCastExecutionType getType() {
        return AscensionHeldCastExecutionTypes.PROJECTILE_RELEASE.get();
    }

    @Override
    public void execute(HeldCastExecutionContext context) {
        HeldCastProjectileManager.spawn(context, this);
    }
}
