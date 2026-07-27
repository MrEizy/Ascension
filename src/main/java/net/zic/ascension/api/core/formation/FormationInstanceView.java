package net.zic.ascension.api.core.formation;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public interface FormationInstanceView {
    UUID runtimeId();

    UUID ownerId();

    Identifier definitionId();

    Vec3 center();

    Map<Identifier, Vec3> anchors();

    long expiresAt();
}
