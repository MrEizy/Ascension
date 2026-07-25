package net.zic.ascension.api.core.effect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum SkillEffectStackingPolicy implements StringRepresentable {
    REFRESH("refresh"),
    STRONGER_REPLACES("stronger_replaces"),
    STACK("stack");

    public static final Codec<SkillEffectStackingPolicy> CODEC = StringRepresentable.fromEnum(SkillEffectStackingPolicy::values);
    private final String name;

    SkillEffectStackingPolicy(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
