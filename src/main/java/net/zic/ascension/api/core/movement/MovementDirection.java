package net.zic.ascension.api.core.movement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MovementDirection implements StringRepresentable {
    LOOK("look"),
    TOWARD_TARGET("toward_target"),
    AWAY_FROM_TARGET("away_from_target");

    public static final Codec<MovementDirection> CODEC =
            StringRepresentable.fromEnum(MovementDirection::values);

    private final String name;

    MovementDirection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
