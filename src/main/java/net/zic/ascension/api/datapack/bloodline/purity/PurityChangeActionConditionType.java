package net.zic.ascension.api.datapack.bloodline.purity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.datapack.TypeRegistries;

public abstract class PurityChangeActionConditionType {
    public abstract MapCodec<? extends PurityChangeActionCondition> codec();


    public static Codec<PurityChangeActionCondition> PURITY_CHANGE_ACTION_CONDITION_CODEC = TypeRegistries.PURITY_CHANGE_ACTION_CONDITION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    PurityChangeActionCondition::getType,
                    PurityChangeActionConditionType::codec
            );
}
