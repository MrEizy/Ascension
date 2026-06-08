package net.zic.ascension.datapack.technique.realm_change.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.core.technique.realm_change.condition.EveryMajorRealmCondition;
import net.zic.ascension.core.technique.realm_change.condition.EveryMajorRealmInCondition;
import net.zic.ascension.core.technique.realm_change.condition.EveryMinorRealmInCondition;

public class EveryMinorRealmInConditionType extends ProgressActionConditionType {
    @Override
    public MapCodec<? extends ProgressActionCondition> codec() {
        return RecordCodecBuilder.<EveryMinorRealmInCondition>mapCodec(
                instance->
                    instance.group(
                            Codec.INT.listOf().fieldOf("realms").forGetter(EveryMinorRealmInCondition::minorRealms)
                    ).apply(instance,EveryMinorRealmInCondition::new)
        );
    }
}
