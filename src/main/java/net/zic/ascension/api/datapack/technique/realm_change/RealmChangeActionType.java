package net.zic.ascension.api.datapack.technique.realm_change;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeAction;

public abstract class RealmChangeActionType {

    public abstract MapCodec<? extends RealmChangeAction> codec();


    public static Codec<RealmChangeAction> REALM_CHANGE_ACTION_CODEC = TypeRegistries.REALM_CHANGE_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    RealmChangeAction::getType,
                    RealmChangeActionType::codec
            );
}
