package net.zic.ascension.api.core.targeting;

import com.mojang.serialization.MapCodec;

public final class TargetingType {
    private final MapCodec<? extends TargetingDefinition> codec;

    public TargetingType(MapCodec<? extends TargetingDefinition> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends TargetingDefinition> codec() {
        return codec;
    }
}
