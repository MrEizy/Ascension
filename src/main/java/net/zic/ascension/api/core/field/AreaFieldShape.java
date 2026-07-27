package net.zic.ascension.api.core.field;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum AreaFieldShape implements StringRepresentable {
    SPHERE("sphere"),
    CYLINDER("cylinder");

    public static final Codec<AreaFieldShape> CODEC = StringRepresentable.fromEnum(AreaFieldShape::values);

    private final String name;

    AreaFieldShape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
