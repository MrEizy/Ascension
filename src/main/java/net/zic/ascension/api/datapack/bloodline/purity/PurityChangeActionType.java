package net.zic.ascension.api.datapack.bloodline.purity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.technique.realm_change.RealmChangeActionConditionType;

public abstract class PurityChangeActionType {
    public abstract MapCodec<? extends PurityChangeAction> codec();


    public static Codec<PurityChangeAction> PURITY_CHANGE_ACTION_CODEC = TypeRegistries.PURITY_CHANGE_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    PurityChangeAction::getType,
                    PurityChangeActionType::codec
            );
}
