package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;

import java.util.List;

public record SkillExecutionDefinition(
        TargetingDefinition targeting,
        boolean requireTargets,
        List<SkillExecutionFeature> features
) {
    public static final MapCodec<SkillExecutionDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetingDefinition.CODEC.fieldOf("targeting").forGetter(SkillExecutionDefinition::targeting),
            Codec.BOOL.optionalFieldOf("require_targets", true).forGetter(SkillExecutionDefinition::requireTargets),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features", List.of()).forGetter(SkillExecutionDefinition::features)
    ).apply(instance, SkillExecutionDefinition::new));

    public SkillExecutionDefinition {
        features = features == null ? List.of() : List.copyOf(features);
    }
}
