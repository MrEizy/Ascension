package net.zic.ascension.api.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.targeting.TargetingDefinition;

import java.util.List;

public record SkillExecutionDefinition(
        TargetingDefinition targeting,
        boolean requireTargets,
        List<SkillExecutionFeature> casterFeatures,
        List<SkillExecutionFeature> targetFeatures
) {
    public static final MapCodec<SkillExecutionDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetingDefinition.CODEC.fieldOf("targeting").forGetter(SkillExecutionDefinition::targeting),
            Codec.BOOL.optionalFieldOf("require_targets", true).forGetter(SkillExecutionDefinition::requireTargets),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("caster_features", List.of())
                    .forGetter(SkillExecutionDefinition::casterFeatures),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("target_features", List.of())
                    .forGetter(SkillExecutionDefinition::targetFeatures)
    ).apply(instance, SkillExecutionDefinition::new));

    public SkillExecutionDefinition {
        casterFeatures = casterFeatures == null ? List.of() : List.copyOf(casterFeatures);
        targetFeatures = targetFeatures == null ? List.of() : List.copyOf(targetFeatures);
    }
}
