package net.zic.ascension.api.core.construct;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface OwnerBoundConstructView {
    UUID runtimeId();

    UUID ownerId();

    Identifier definitionId();

    double stability();

    double maximumStability();

    Vec3 position();

    long expiresAt();
}
