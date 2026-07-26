package net.zic.ascension.api.core.targeting;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface TargetingDefinition {
    Codec<TargetingDefinition> CODEC = TypeRegistries.TARGETING_TYPE_REGISTRY.byNameCodec().dispatch(
            TargetingDefinition::getType,
            TargetingType::codec
    );

    TargetingType getType();

    TargetingResult resolve(TargetingContext context);
}
