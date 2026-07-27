package net.zic.ascension.api.core.field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record AreaFieldDefinition(
        AreaFieldShape shape,
        ScaledValue radius,
        ScaledValue height,
        ScaledValue duration,
        int tickInterval,
        TargetFilterDefinition filter,
        List<SkillExecutionFeature> enterFeatures,
        List<SkillExecutionFeature> stayFeatures,
        List<SkillExecutionFeature> exitFeatures,
        Optional<Identifier> visual
) {
    public static final Codec<AreaFieldDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AreaFieldShape.CODEC.optionalFieldOf("shape", AreaFieldShape.CYLINDER)
                    .forGetter(AreaFieldDefinition::shape),
            ScaledValue.CODEC.codec().fieldOf("radius").forGetter(AreaFieldDefinition::radius),
            ScaledValue.CODEC.codec().optionalFieldOf("height", ScaledValue.constant(4.0D))
                    .forGetter(AreaFieldDefinition::height),
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(AreaFieldDefinition::duration),
            Codec.intRange(1, 1200).optionalFieldOf("tick_interval", 20)
                    .forGetter(AreaFieldDefinition::tickInterval),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(AreaFieldDefinition::filter),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("enter_features", List.of())
                    .forGetter(AreaFieldDefinition::enterFeatures),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("stay_features", List.of())
                    .forGetter(AreaFieldDefinition::stayFeatures),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("exit_features", List.of())
                    .forGetter(AreaFieldDefinition::exitFeatures),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(AreaFieldDefinition::visual)
    ).apply(instance, AreaFieldDefinition::new));

    public AreaFieldDefinition {
        shape = shape == null ? AreaFieldShape.CYLINDER : shape;
        filter = filter == null ? TargetFilterDefinition.hostile() : filter;
        enterFeatures = enterFeatures == null ? List.of() : List.copyOf(enterFeatures);
        stayFeatures = stayFeatures == null ? List.of() : List.copyOf(stayFeatures);
        exitFeatures = exitFeatures == null ? List.of() : List.copyOf(exitFeatures);
        visual = visual == null ? Optional.empty() : visual;
    }
}
