package net.zic.ascension.configuration.mobs.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.configuration.ConfigurationRegistries;

public abstract class MobConfigurationConditionType {
    public abstract MapCodec<? extends MobConfigurationCondition> codec();


    public static Codec<MobConfigurationCondition> MOB_CONFIGURATION_CONDITION_CODEC = ConfigurationRegistries.MOB_CONFIGURATION_CONDITION_TYPES.byNameCodec()
            .dispatch(
                    MobConfigurationCondition::getType,
                    MobConfigurationConditionType::codec
            );
}
