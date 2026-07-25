package net.zic.ascension.api.core.skill.castable.held.feature;

import com.mojang.serialization.MapCodec;

public final class HeldCastReleaseFeatureType {
    private final MapCodec<? extends HeldCastReleaseFeature> codec;

    public HeldCastReleaseFeatureType(MapCodec<? extends HeldCastReleaseFeature> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends HeldCastReleaseFeature> codec() {
        return codec;
    }
}
