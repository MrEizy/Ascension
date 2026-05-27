package net.zic.ascension.api.datapack.technique.realm_change;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.core.technique.realm_change.ListenerAction;

public abstract class ListenerActionType {

    public abstract MapCodec<? extends ListenerAction> codec();


    public static Codec<ListenerAction> LISTENER_ACTION_CODEC = TypeRegistries.LISTENER_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    ListenerAction::getType,
                    ListenerActionType::codec
            );
}
