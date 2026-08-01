package net.zic.ascension.api.ascension.datapack.progresison;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class ProgressActionConditionType {
    public abstract MapCodec<? extends ProgressActionCondition> codec();


    public static Codec<ProgressActionCondition> PROGRESS_ACTION_CONDITION_CODEC = TypeRegistries.PROGRESS_ACTION_CONDITION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    ProgressActionCondition::getType,
                    ProgressActionConditionType::codec
            );
}
