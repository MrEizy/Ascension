package net.zic.ascension.api.datapack.technique.realm_change.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerAction;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerCondition;

public abstract class ListenerConditionType {
    public abstract MapCodec<? extends ListenerCondition> codec();


    public static Codec<ListenerCondition> LISTENER_ACTION_CODEC = TypeRegistries.LISTENER_CONDITIONN_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    ListenerCondition::getType,
                    ListenerConditionType::codec
            );
}
