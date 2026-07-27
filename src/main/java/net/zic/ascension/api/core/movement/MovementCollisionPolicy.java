package net.zic.ascension.api.core.movement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MovementCollisionPolicy implements StringRepresentable {
    FAIL("fail"),
    STOP_BEFORE_COLLISION("stop_before_collision");

    public static final Codec<MovementCollisionPolicy> CODEC =
            StringRepresentable.fromEnum(MovementCollisionPolicy::values);

    private final String name;

    MovementCollisionPolicy(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
