package net.zic.ascension.datapack.technique.realm_change.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.core.technique.realm_change.condition.EveryMinorRealmInCondition;
import net.zic.ascension.core.technique.realm_change.condition.EveryRealmCondition;
import net.zic.ascension.core.technique.realm_change.condition.EveryRealmInCondition;

public class EveryRealmInConditionType extends ProgressActionConditionType {
    @Override
    public MapCodec<? extends ProgressActionCondition> codec() {
        return RecordCodecBuilder.<EveryRealmInCondition>mapCodec(
                instance->
                        instance.group(
                                Codec.unboundedMap(Codec.INT,Codec.INT.listOf()).fieldOf("realms").forGetter(EveryRealmInCondition::realms)
                        ).apply(instance, EveryRealmInCondition::new)
        );
    }
}