package net.zic.ascension.api.ascension.value;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface ScaledValueSource {
    Codec<ScaledValueSource> CODEC = TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY.byNameCodec().dispatch(ScaledValueSource::getType, CodecType<ScaledValueSource>::codec);

    CodecType<ScaledValueSource> getType();

    double resolve(ScaledValueContext context);
}
