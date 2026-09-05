package net.zic.ascension.configuration.item.qi_capacity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Map;
import java.util.function.Function;

public record TierCapacityDefinition(Map<String,Long> capacity){
    public static Codec<TierCapacityDefinition> CODEC = Codec.unboundedMap(
            Codec.STRING,
            TierCapacityDefinition.longRange(0L,Long.MAX_VALUE)
    ).xmap(TierCapacityDefinition::new,TierCapacityDefinition::capacity);


    //TODO move to util
    public static Codec<Long> longRange(long min, long max) {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(min ,max);
        return Codec.LONG.flatXmap(checker, checker);
    }
}
