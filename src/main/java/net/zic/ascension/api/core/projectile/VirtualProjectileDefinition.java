package net.zic.ascension.api.core.projectile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record VirtualProjectileDefinition(
        ScaledValue speed,
        ScaledValue range,
        double gravity,
        double hitRadius,
        int pierces,
        TargetFilterDefinition filter,
        Optional<Identifier> flightParticle,
        List<ProjectileBehavior> behaviors,
        List<SkillExecutionFeature> onEntityHit,
        List<SkillExecutionFeature> onBlockHit,
        List<SkillExecutionFeature> onExpire,
        Optional<Identifier> visual
) {
    public static final Codec<VirtualProjectileDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("speed").forGetter(VirtualProjectileDefinition::speed),
            ScaledValue.CODEC.codec().fieldOf("range").forGetter(VirtualProjectileDefinition::range),
            Codec.DOUBLE.optionalFieldOf("gravity", 0.0D).forGetter(VirtualProjectileDefinition::gravity),
            Codec.DOUBLE.optionalFieldOf("hit_radius", 0.3D).forGetter(VirtualProjectileDefinition::hitRadius),
            Codec.intRange(0, 64).optionalFieldOf("pierces", 0).forGetter(VirtualProjectileDefinition::pierces),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(VirtualProjectileDefinition::filter),
            Identifier.CODEC.optionalFieldOf("flight_particle").forGetter(VirtualProjectileDefinition::flightParticle),
            ProjectileBehavior.CODEC.listOf().optionalFieldOf("behaviors", List.of())
                    .forGetter(VirtualProjectileDefinition::behaviors),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_entity_hit", List.of())
                    .forGetter(VirtualProjectileDefinition::onEntityHit),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_block_hit", List.of())
                    .forGetter(VirtualProjectileDefinition::onBlockHit),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_expire", List.of())
                    .forGetter(VirtualProjectileDefinition::onExpire),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(VirtualProjectileDefinition::visual)
    ).apply(instance, VirtualProjectileDefinition::new));

    public VirtualProjectileDefinition {
        gravity = Double.isFinite(gravity) ? Math.clamp(gravity, -4.0D, 4.0D) : 0.0D;
        hitRadius = Double.isFinite(hitRadius) ? Math.clamp(hitRadius, 0.0D, 4.0D) : 0.3D;
        filter = filter == null ? TargetFilterDefinition.hostile() : filter;
        flightParticle = flightParticle == null ? Optional.empty() : flightParticle;
        behaviors = behaviors == null ? List.of() : List.copyOf(behaviors);
        onEntityHit = onEntityHit == null ? List.of() : List.copyOf(onEntityHit);
        onBlockHit = onBlockHit == null ? List.of() : List.copyOf(onBlockHit);
        onExpire = onExpire == null ? List.of() : List.copyOf(onExpire);
        visual = visual == null ? Optional.empty() : visual;
    }
}
