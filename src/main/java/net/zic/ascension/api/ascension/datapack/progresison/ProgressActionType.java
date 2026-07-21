package net.zic.ascension.api.ascension.datapack.progresison;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class ProgressActionType {
    public abstract MapCodec<? extends ProgressAction> codec();


    public static Codec<ProgressAction> PROGRESS_ACTION_CODEC = TypeRegistries.PROGRESS_ACTION_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    ProgressAction::getType,
                    ProgressActionType::codec
            );
}
