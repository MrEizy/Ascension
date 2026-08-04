package net.zic.ascension.api.ascension.core.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ProjectileImpactResponse implements StringRepresentable {
    NONE("none"),
    STOP("stop"),
    DISCARD("discard"),
    DEFLECT("deflect");

    public static final Codec<ProjectileImpactResponse> CODEC = StringRepresentable.fromEnum(ProjectileImpactResponse::values);
    private final String name;

    ProjectileImpactResponse(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
