package net.zic.ascension.api.core.projectile;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public interface VirtualProjectileAccess {
    UUID runtimeId();

    UUID ownerId();

    Identifier skillId();

    Vec3 position();

    void setPosition(Vec3 position);

    Vec3 velocity();

    void setVelocity(Vec3 velocity);

    UUID targetId();

    void setTargetId(UUID targetId);

    double travelled();

    int pierces();

    Set<UUID> hitEntities();
}
