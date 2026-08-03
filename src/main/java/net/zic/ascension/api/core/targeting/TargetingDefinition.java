package net.zic.ascension.api.core.targeting;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface TargetingDefinition {
    Codec<TargetingDefinition> CODEC = TypeRegistries.TARGETING_TYPE_REGISTRY.byNameCodec().dispatch(
            TargetingDefinition::getType,
            CodecType<TargetingDefinition>::codec
    );

    CodecType<TargetingDefinition> getType();

    TargetingResult resolve(TargetingContext context);
}
