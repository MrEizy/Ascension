package net.zic.ascension.api.ascension.core.projectile;

import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Optional;

/** Defines a virtual projectile and its optional registry-backed visual. */
public record VirtualProjectileDefinition(
        ScaledValue speed,
        ScaledValue range,
        double gravity,
        double hitRadius,
        int pierces,
        TargetingDefinition.Filter filter,
        Optional<Identifier> flightParticle,
        List<ProjectileBehavior> behaviors,
        List<SkillAction> onEntityHit,
        List<SkillAction> onBlockHit,
        List<SkillAction> onExpire,
        Optional<Identifier> visual
) {
    public static final Codec<VirtualProjectileDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.fieldOf("speed").forGetter(VirtualProjectileDefinition::speed),
            ScaledValue.COMPACT_CODEC.fieldOf("range").forGetter(VirtualProjectileDefinition::range),
            Codec.DOUBLE.optionalFieldOf("gravity", 0.0D).forGetter(VirtualProjectileDefinition::gravity),
            Codec.DOUBLE.optionalFieldOf("hit_radius", 0.3D).forGetter(VirtualProjectileDefinition::hitRadius),
            Codec.intRange(0, 64).optionalFieldOf("pierces", 0).forGetter(VirtualProjectileDefinition::pierces),
            TargetingDefinition.Filter.CODEC.codec().optionalFieldOf("filter", TargetingDefinition.Filter.hostile()).forGetter(VirtualProjectileDefinition::filter),
            Identifier.CODEC.optionalFieldOf("flight_particle").forGetter(VirtualProjectileDefinition::flightParticle),
            ProjectileBehavior.CODEC.listOf().optionalFieldOf("behaviors", List.of()).forGetter(VirtualProjectileDefinition::behaviors),
            SkillAction.CODEC.listOf().optionalFieldOf("on_entity_hit", List.of()).forGetter(VirtualProjectileDefinition::onEntityHit),
            SkillAction.CODEC.listOf().optionalFieldOf("on_block_hit", List.of()).forGetter(VirtualProjectileDefinition::onBlockHit),
            SkillAction.CODEC.listOf().optionalFieldOf("on_expire", List.of()).forGetter(VirtualProjectileDefinition::onExpire),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(VirtualProjectileDefinition::visual)
    ).apply(instance, VirtualProjectileDefinition::new));

    public VirtualProjectileDefinition {
        gravity = Double.isFinite(gravity) ? Math.clamp(gravity, -4.0D, 4.0D) : 0.0D;
        hitRadius = Double.isFinite(hitRadius) ? Math.clamp(hitRadius, 0.0D, 4.0D) : 0.3D;
        filter = filter == null ? TargetingDefinition.Filter.hostile() : filter;
        flightParticle = flightParticle == null ? Optional.empty() : flightParticle;
        behaviors = behaviors == null ? List.of() : List.copyOf(behaviors);
        onEntityHit = onEntityHit == null ? List.of() : List.copyOf(onEntityHit);
        onBlockHit = onBlockHit == null ? List.of() : List.copyOf(onBlockHit);
        onExpire = onExpire == null ? List.of() : List.copyOf(onExpire);
        visual = visual == null ? Optional.empty() : visual;
    }
    public enum Direction implements StringRepresentable {
        LOOK("look"),
        TARGET("target");

        public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);
        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
