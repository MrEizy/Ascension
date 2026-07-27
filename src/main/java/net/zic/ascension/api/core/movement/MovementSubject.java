package net.zic.ascension.api.core.movement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MovementSubject implements StringRepresentable {
    CASTER("caster"),
    TARGET("target");

    public static final Codec<MovementSubject> CODEC = StringRepresentable.fromEnum(MovementSubject::values);

    private final String name;

    MovementSubject(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
