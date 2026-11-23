package net.zic.ascension.configuration.mobs.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightedCondition(MobConfigurationCondition condition, int weight) {
    public static final Codec<WeightedCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobConfigurationConditionType.MOB_CONFIGURATION_CONDITION_CODEC.fieldOf("condition").forGetter(WeightedCondition::condition),
            Codec.INT.fieldOf("weight").forGetter(WeightedCondition::weight)
    ).apply(instance, WeightedCondition::new));
}
