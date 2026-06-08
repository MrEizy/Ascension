package net.zic.ascension.impl.datapack.technique.realm_change.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.core.technique.realm_change.condition.EveryMajorRealmInCondition;

public class EveryMajorRealmInConditionType extends ProgressActionConditionType {
    @Override
    public MapCodec<? extends ProgressActionCondition> codec() {
        return RecordCodecBuilder.<EveryMajorRealmInCondition>mapCodec(
                instance->
                        instance.group(
                                Codec.INT.listOf().fieldOf("realms").forGetter(EveryMajorRealmInCondition::majorRealms)
                        ).apply(instance,EveryMajorRealmInCondition::new)
        );
    }
}