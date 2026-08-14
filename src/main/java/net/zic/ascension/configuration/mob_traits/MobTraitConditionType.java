package net.zic.ascension.configuration.mob_traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.configuration.ConfigurationRegistries;

public abstract class MobTraitConditionType {
    public abstract MapCodec<? extends MobTraitCondition> codec();


    public static Codec<MobTraitCondition> MOB_TRAIT_CONDITION_CODEC = ConfigurationRegistries.MOB_TRAIT_CONDITION_TYPES.byNameCodec()
            .dispatch(
                    MobTraitCondition::getType,
                    MobTraitConditionType::codec
            );
}
