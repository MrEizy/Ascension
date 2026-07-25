package net.zic.ascension.api.value.source;

import com.mojang.serialization.MapCodec;

public final class ScaledValueSourceType {
    private final MapCodec<? extends ScaledValueSource> codec;

    public ScaledValueSourceType(MapCodec<? extends ScaledValueSource> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends ScaledValueSource> codec() {
        return codec;
    }
}
