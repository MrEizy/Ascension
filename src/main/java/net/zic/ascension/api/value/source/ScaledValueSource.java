package net.zic.ascension.api.value.source;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.value.ScaledValueContext;

public interface ScaledValueSource {
    Codec<ScaledValueSource> CODEC = TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY.byNameCodec().dispatch(ScaledValueSource::getType, ScaledValueSourceType::codec);

    ScaledValueSourceType getType();

    double resolve(ScaledValueContext context);
}
