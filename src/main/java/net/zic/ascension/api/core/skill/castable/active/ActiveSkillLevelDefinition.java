package net.zic.ascension.api.core.skill.castable.active;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.SkillExecutionDefinition;
import net.zic.ascension.api.value.ScaledValue;

import java.util.List;

public record ActiveSkillLevelDefinition(
        SkillExecutionDefinition execution,
        List<ActiveSkillCostDefinition> costs,
        ScaledValue cooldown
) {
    public static final MapCodec<ActiveSkillLevelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillExecutionDefinition.CODEC.fieldOf("execution").forGetter(ActiveSkillLevelDefinition::execution),
            ActiveSkillCostDefinition.CODEC.codec().listOf().optionalFieldOf("costs", List.of())
                    .forGetter(ActiveSkillLevelDefinition::costs),
            ScaledValue.CODEC.codec().optionalFieldOf("cooldown", ScaledValue.constant(0.0D))
                    .forGetter(ActiveSkillLevelDefinition::cooldown)
    ).apply(instance, ActiveSkillLevelDefinition::new));

    public ActiveSkillLevelDefinition {
        costs = costs == null ? List.of() : List.copyOf(costs);
    }
}
