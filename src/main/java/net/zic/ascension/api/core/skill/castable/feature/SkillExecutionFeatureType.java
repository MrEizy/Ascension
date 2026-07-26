package net.zic.ascension.api.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;

public final class SkillExecutionFeatureType {
    private final MapCodec<? extends SkillExecutionFeature> codec;

    public SkillExecutionFeatureType(MapCodec<? extends SkillExecutionFeature> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends SkillExecutionFeature> codec() {
        return codec;
    }
}
