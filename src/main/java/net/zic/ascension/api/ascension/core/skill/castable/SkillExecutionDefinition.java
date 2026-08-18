package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;

import java.util.List;

public record SkillExecutionDefinition(
        TargetingDefinition targeting,
        boolean requireTargets,
        List<SkillAction> actions
) {
    public static final MapCodec<SkillExecutionDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetingDefinition.CODEC.fieldOf("targeting").forGetter(SkillExecutionDefinition::targeting),
            Codec.BOOL.optionalFieldOf("require_targets", true).forGetter(SkillExecutionDefinition::requireTargets),
            SkillAction.CODEC.listOf().optionalFieldOf("features", List.of()).forGetter(SkillExecutionDefinition::actions)
    ).apply(instance, SkillExecutionDefinition::new));

    public SkillExecutionDefinition {
        actions = actions == null ? List.of() : List.copyOf(actions);
    }
}
