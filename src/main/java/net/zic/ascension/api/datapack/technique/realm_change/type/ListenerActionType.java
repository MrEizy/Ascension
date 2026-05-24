package net.zic.ascension.api.datapack.technique.realm_change.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.technique.realm_change.ListenerAction;

public abstract class ListenerActionType {

    public abstract MapCodec<? extends ListenerAction> codec();


    public static Codec<ListenerAction> LISTENER_ACTION_CODEC = TypeRegistries.LISTENER_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    ListenerAction::getType,
                    ListenerActionType::codec
            );
}
