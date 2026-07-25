package net.zic.ascension.api.core.effect;

import com.mojang.serialization.MapCodec;

public final class SkillEffectModuleType {
    private final MapCodec<? extends SkillEffectModule> codec;

    public SkillEffectModuleType(MapCodec<? extends SkillEffectModule> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends SkillEffectModule> codec() {
        return codec;
    }
}
