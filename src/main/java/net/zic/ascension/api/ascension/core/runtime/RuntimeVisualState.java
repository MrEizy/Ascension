package net.zic.ascension.api.ascension.core.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public record RuntimeVisualState(
        UUID runtimeId,
        Identifier visual,
        UUID ownerId,
        Vec3 position,
        Vec3 offset,
        List<Vec3> points,
        List<Link> links,
        long expiresAt,
        int stage,
        int flags,
        float progress,
        long seed,
        double primaryValue,
        double secondaryValue,
        float scale,
        double spin,
        int tint,
        int secondaryTint,
        RuntimeVisualDefinition definition
) {
    public static final int OWNER_RELATIVE = 1;
    public static final int ROTATE_WITH_OWNER = 1 << 1;
    public static final int WHITE_TINT = 0xFFFFFFFF;

    public RuntimeVisualState {
        position = position == null ? Vec3.ZERO : position;
        offset = offset == null ? Vec3.ZERO : offset;
        points = points == null ? List.of() : List.copyOf(points);
        links = links == null ? List.of() : List.copyOf(links);
        stage = Math.max(0, stage);
        progress = Math.clamp(progress, 0.0F, 1.0F);
        scale = Float.isFinite(scale) ? Math.max(0.0001F, scale) : 1.0F;
        spin = Double.isFinite(spin) ? spin : 0.0D;
    }

    public RuntimeVisualState(
            UUID runtimeId,
            Identifier visual,
            UUID ownerId,
            Vec3 position,
            Vec3 offset,
            List<Vec3> points,
            List<Link> links,
            long expiresAt,
            int stage,
            int flags,
            float progress,
            long seed,
            double primaryValue,
            double secondaryValue,
            RuntimeVisualDefinition definition
    ) {
        this(runtimeId, visual, ownerId, position, offset, points, links, expiresAt, stage, flags, progress, seed,
                primaryValue, secondaryValue, 1.0F, 0.0D, WHITE_TINT, WHITE_TINT, definition);
    }

    public RuntimeVisualState(
            UUID runtimeId,
            Identifier visual,
            UUID ownerId,
            Vec3 position,
            Vec3 offset,
            List<Vec3> points,
            List<Link> links,
            long expiresAt,
            int stage,
            int flags,
            float progress,
            long seed,
            double primaryValue,
            double secondaryValue
    ) {
        this(runtimeId, visual, ownerId, position, offset, points, links, expiresAt, stage, flags, progress, seed,
                primaryValue, secondaryValue, 1.0F, 0.0D, WHITE_TINT, WHITE_TINT, null);
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    public enum Action {
        SPAWN,
        UPDATE,
        REMOVE
    }

    public record Link(int from, int to) {
        public Link {
            from = Math.max(0, from);
            to = Math.max(0, to);
        }
    }
}
