package net.zic.ascension.datapack.bloodline.purity.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeActionCondition;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionConditionType;
import net.zic.ascension.core.bloodline.purity.condition.OnPurityInRangeCondition;

public class OnPurityInRangeConditionType extends PurityChangeActionConditionType {
    @Override
    public MapCodec<? extends PurityChangeActionCondition> codec() {
        return RecordCodecBuilder.<OnPurityInRangeCondition>mapCodec(instance ->
                instance.group(
                        Codec.INT.fieldOf("start").forGetter(OnPurityInRangeCondition::start),
                        Codec.INT.fieldOf("end").forGetter(OnPurityInRangeCondition::end)
                ).apply(instance, OnPurityInRangeCondition::new)
        );
    }
}
