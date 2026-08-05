package net.zic.ascension.api.ascension.core.projectile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;

import java.util.List;
import java.util.Optional;

public record NormalProjectileDefinition(
        Optional<Identifier> requiredSkill,
        Optional<Identifier> projectileTag,
        List<Identifier> projectileTypes,
        ScaledValue damageMultiplier,
        ScaledValue bonusDamage,
        List<Identifier> classifications,
        Optional<Identifier> path,
        Optional<Identifier> technique,
        double fullChargeSpeed,
        Optional<Steering> steering,
        Optional<Stagger> stagger
) {
    public static final Codec<NormalProjectileDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("required_skill").forGetter(NormalProjectileDefinition::requiredSkill),
            Identifier.CODEC.optionalFieldOf("projectile_tag").forGetter(NormalProjectileDefinition::projectileTag),
            Identifier.CODEC.listOf().optionalFieldOf("projectile_types", List.of())
                    .forGetter(NormalProjectileDefinition::projectileTypes),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("damage_multiplier", ScaledValue.constant(1.0D))
                    .forGetter(NormalProjectileDefinition::damageMultiplier),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("bonus_damage", ScaledValue.constant(0.0D))
                    .forGetter(NormalProjectileDefinition::bonusDamage),
            Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of())
                    .forGetter(NormalProjectileDefinition::classifications),
            Identifier.CODEC.optionalFieldOf("path").forGetter(NormalProjectileDefinition::path),
            Identifier.CODEC.optionalFieldOf("technique").forGetter(NormalProjectileDefinition::technique),
            Codec.doubleRange(0.01D, 128.0D).optionalFieldOf("full_charge_speed", 3.0D)
                    .forGetter(NormalProjectileDefinition::fullChargeSpeed),
            Steering.CODEC.optionalFieldOf("steering").forGetter(NormalProjectileDefinition::steering),
            Stagger.CODEC.optionalFieldOf("stagger").forGetter(NormalProjectileDefinition::stagger)
    ).apply(instance, NormalProjectileDefinition::new));

    public NormalProjectileDefinition {
        requiredSkill = requiredSkill == null ? Optional.empty() : requiredSkill;
        projectileTag = projectileTag == null ? Optional.empty() : projectileTag;
        projectileTypes = projectileTypes == null ? List.of() : List.copyOf(projectileTypes);
        classifications = classifications == null ? List.of() : List.copyOf(classifications);
        path = path == null ? Optional.empty() : path;
        technique = technique == null ? Optional.empty() : technique;
        steering = steering == null ? Optional.empty() : steering;
        stagger = stagger == null ? Optional.empty() : stagger;
    }

    public record Steering(
            DefinitionRef<SkillEffectDefinition> effect,
            Optional<Identifier> sourceSkill,
            ScaledValue range,
            ScaledValue turnRate,
            int reacquireInterval,
            boolean ownerScoped
    ) {
        public static final Codec<Steering> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DefinitionRef.codec(SkillEffectDefinition.CODEC).fieldOf("effect").forGetter(Steering::effect),
                Identifier.CODEC.optionalFieldOf("source_skill").forGetter(Steering::sourceSkill),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("range", ScaledValue.constant(24.0D))
                        .forGetter(Steering::range),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("turn_rate", ScaledValue.constant(0.12D))
                        .forGetter(Steering::turnRate),
                Codec.intRange(1, 200).optionalFieldOf("reacquire_interval", 4)
                        .forGetter(Steering::reacquireInterval),
                Codec.BOOL.optionalFieldOf("owner_scoped", true).forGetter(Steering::ownerScoped)
        ).apply(instance, Steering::new));

        public Steering {
            sourceSkill = sourceSkill == null ? Optional.empty() : sourceSkill;
        }
    }

    public record Stagger(
            DefinitionRef<StaggerDefinition> profile,
            ScaledValue amount,
            boolean scaleWithDamage
    ) {
        public static final Codec<Stagger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DefinitionRef.codec(StaggerDefinition.CODEC).fieldOf("profile").forGetter(Stagger::profile),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Stagger::amount),
                Codec.BOOL.optionalFieldOf("scale_with_damage", false).forGetter(Stagger::scaleWithDamage)
        ).apply(instance, Stagger::new));
    }
    public enum ImpactResponse implements StringRepresentable {
        NONE("none"),
        STOP("stop"),
        DISCARD("discard"),
        DEFLECT("deflect");

        public static final Codec<ImpactResponse> CODEC = StringRepresentable.fromEnum(ImpactResponse::values);
        private final String name;

        ImpactResponse(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
