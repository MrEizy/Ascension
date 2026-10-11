package net.zic.ascension.api.ascension.datapack.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionAction;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

/**
 * Tells the game how to read/write a TribulationResolutionAction,
 * actions are saved alongside an active tribulation so they must be serializable
 */
public abstract class TribulationResolutionActionType {
    public static final Codec<TribulationResolutionAction> CODEC = TypeRegistries.TRIBULATION_RESOLUTION_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    TribulationResolutionAction::getType,
                    TribulationResolutionActionType::codec
            );

    public abstract MapCodec<? extends TribulationResolutionAction> codec();
}
