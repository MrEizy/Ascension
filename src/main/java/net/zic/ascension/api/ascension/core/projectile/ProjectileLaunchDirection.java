package net.zic.ascension.api.ascension.core.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ProjectileLaunchDirection implements StringRepresentable {
    LOOK("look"),
    TARGET("target");

    public static final Codec<ProjectileLaunchDirection> CODEC =
            StringRepresentable.fromEnum(ProjectileLaunchDirection::values);

    private final String name;

    ProjectileLaunchDirection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
