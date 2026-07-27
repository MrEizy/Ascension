package net.zic.ascension.api.client.visual;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public record RuntimeVisualState(
        UUID runtimeId,
        RuntimeVisualKind kind,
        Identifier visual,
        UUID ownerId,
        Vec3 position,
        Vec3 velocity,
        List<Vec3> points,
        List<RuntimeVisualLink> links,
        long expiresAt,
        int stage,
        int flags,
        float progress,
        long seed,
        double primaryValue,
        double secondaryValue
) {
    public RuntimeVisualState {
        position = position == null ? Vec3.ZERO : position;
        velocity = velocity == null ? Vec3.ZERO : velocity;
        points = points == null ? List.of() : List.copyOf(points);
        links = links == null ? List.of() : List.copyOf(links);
        stage = Math.max(0, stage);
        progress = Math.clamp(progress, 0.0F, 1.0F);
    }
}
