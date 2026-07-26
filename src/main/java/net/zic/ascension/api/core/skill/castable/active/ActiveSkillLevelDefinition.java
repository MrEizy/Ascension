package net.zic.ascension.api.core.skill.castable.active;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.targeting.TargetingDefinition;
import net.zic.ascension.api.value.ScaledValue;

import java.util.List;

public record ActiveSkillLevelDefinition(
        TargetingDefinition targeting,
        boolean requireTargets,
        List<ActiveSkillCostDefinition> costs,
        ScaledValue cooldown,
        List<SkillExecutionFeature> originFeatures,
        List<SkillExecutionFeature> features
) {
    public static final MapCodec<ActiveSkillLevelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetingDefinition.CODEC.fieldOf("targeting").forGetter(ActiveSkillLevelDefinition::targeting),
            Codec.BOOL.optionalFieldOf("require_targets", true).forGetter(ActiveSkillLevelDefinition::requireTargets),
            ActiveSkillCostDefinition.CODEC.codec().listOf().optionalFieldOf("costs", List.of())
                    .forGetter(ActiveSkillLevelDefinition::costs),
            ScaledValue.CODEC.codec().optionalFieldOf("cooldown", ScaledValue.constant(0.0D))
                    .forGetter(ActiveSkillLevelDefinition::cooldown),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("origin_features", List.of())
                    .forGetter(ActiveSkillLevelDefinition::originFeatures),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features", List.of())
                    .forGetter(ActiveSkillLevelDefinition::features)
    ).apply(instance, ActiveSkillLevelDefinition::new));

    public ActiveSkillLevelDefinition {
        costs = costs == null ? List.of() : List.copyOf(costs);
        originFeatures = originFeatures == null ? List.of() : List.copyOf(originFeatures);
        features = features == null ? List.of() : List.copyOf(features);
    }
}
