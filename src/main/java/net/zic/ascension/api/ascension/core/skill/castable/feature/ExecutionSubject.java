package net.zic.ascension.api.ascension.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ExecutionSubject implements StringRepresentable {
    CASTER("caster"),
    TARGET("target"),
    ORIGIN("origin"),
    POSITION("position");

    public static final Codec<ExecutionSubject> CODEC = StringRepresentable.fromEnum(ExecutionSubject::values);
    private final String name;

    ExecutionSubject(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
