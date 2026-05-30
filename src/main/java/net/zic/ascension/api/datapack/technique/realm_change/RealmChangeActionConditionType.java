package net.zic.ascension.api.datapack.technique.realm_change;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;

public abstract class RealmChangeActionConditionType {
    public abstract MapCodec<? extends RealmChangeActionCondition> codec();


    public static Codec<RealmChangeActionCondition> REALM_CHANGE_ACTION_CONDITION_CODEC = TypeRegistries.REALM_CHANGE_ACTION_CONDITIONN_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    RealmChangeActionCondition::getType,
                    RealmChangeActionConditionType::codec
            );
}
