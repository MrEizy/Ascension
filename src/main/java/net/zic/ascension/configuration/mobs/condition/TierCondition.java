package net.zic.ascension.configuration.mobs.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.PotentialTrait;

import java.util.List;

public record TierCondition(MobConfigurationCondition condition, int weight) {

    public static final Codec<TierCondition> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    MobConfigurationConditionType.MOB_CONFIGURATION_CONDITION_CODEC.fieldOf("condition").forGetter(TierCondition::condition),
                    Codec.INT.fieldOf("weight").forGetter(TierCondition::weight)
            ).apply(instance, TierCondition::new)
    );

}
