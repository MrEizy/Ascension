package net.zic.ascension.configuration.mob_traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.configuration.ConfigurationRegistries;

public abstract class MobTraitDefinitionType {
    public abstract MapCodec<? extends MobTraitDefinition> codec();


    public static Codec<MobTraitDefinition> MOB_TRAIT_CODEC = ConfigurationRegistries.MOB_TRAIT_TYPES.byNameCodec()
            .dispatch(
                    MobTraitDefinition::getType,
                    MobTraitDefinitionType::codec
            );
}
