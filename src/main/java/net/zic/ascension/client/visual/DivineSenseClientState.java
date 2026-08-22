package net.zic.ascension.client.visual;

import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DivineSenseClientState {
    private static final DivineSenseClientState INSTANCE = new DivineSenseClientState();

    private boolean active;
    private long startTimeMs;
    private Vec3 center = Vec3.ZERO;
    private float radius;
    private int durationTicks;
    private int color = 0xFFFFFF;
    private final Set<Integer> highlighted = new HashSet<>();

    private DivineSenseClientState() {
    }

    public static DivineSenseClientState get() {
        return INSTANCE;
    }

    public void start(Vec3 center, float radius, int durationTicks, int color, List<Integer> entityIds) {
        this.center = center;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.color = color;
        this.startTimeMs = System.currentTimeMillis();
        this.active = true;
        this.highlighted.clear();
        this.highlighted.addAll(entityIds);
    }

    public boolean isActive() {
        return active && elapsedMs() < durationTicks * 50L;
    }

    public boolean isHighlighted(int entityId) {
        return isActive() && highlighted.contains(entityId);
    }

    public Set<Integer> highlightedIds() {
        return Collections.unmodifiableSet(highlighted);
    }

    public Vec3 center() {
        return center;
    }

    public float radius() {
        return radius;
    }

    public int color() {
        return color;
    }

    public long startTimeMs() {
        return startTimeMs;
    }

    public int durationTicks() {
        return durationTicks;
    }

    private long elapsedMs() {
        return System.currentTimeMillis() - startTimeMs;
    }
}