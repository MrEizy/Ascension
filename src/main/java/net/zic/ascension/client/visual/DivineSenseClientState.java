package net.zic.ascension.client.visual;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DivineSenseClientState {
    private static final DivineSenseClientState INSTANCE = new DivineSenseClientState();
    private static final float PROPAGATION_SWELL = 0.16F;
    private static final float OUTLINE_FLARE_DISTANCE = 2.5F;

    private boolean active;
    private long startTimeNanos;
    private Vec3 center = Vec3.ZERO;
    private float radius;
    private float speed;
    private int durationTicks;
    private int color = 0xFFFFFF;
    private final Set<Integer> highlighted = new HashSet<>();

    private DivineSenseClientState() {
    }

    public static DivineSenseClientState get() {
        return INSTANCE;
    }

    public void start(Vec3 center, float radius, float speed, int durationTicks, int color, List<Integer> entityIds) {
        this.center = center;
        this.radius = radius;
        this.speed = speed;
        this.durationTicks = durationTicks;
        this.color = color;
        this.startTimeNanos = System.nanoTime();
        this.active = true;
        this.highlighted.clear();
        this.highlighted.addAll(entityIds);
    }

    public boolean isActive() {
        return active && elapsedSeconds() < durationTicks / 20.0F;
    }

    public boolean isHighlighted(Entity entity) {
        if (!isActive() || !highlighted.contains(entity.getId())) {
            return false;
        }

        float reach = waveRadius() + Math.max(entity.getBbWidth(), 0.25F) * 0.5F;
        return entity.position().distanceToSqr(center) <= reach * reach;
    }

    public int outlineColor(Entity entity) {
        float distance = (float) Math.sqrt(entity.position().distanceToSqr(center));
        float behindWave = waveRadius() - distance;
        float flare = isWaveActive() && behindWave >= 0.0F
                ? 1.0F - Math.clamp(behindWave / OUTLINE_FLARE_DISTANCE, 0.0F, 1.0F)
                : 0.0F;

        float mix = flare * 0.55F;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Math.round(red + (255 - red) * mix);
        green = Math.round(green + (255 - green) * mix);
        blue = Math.round(blue + (255 - blue) * mix);

        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    public Vec3 center() {
        return center;
    }

    public float radius() {
        return radius;
    }

    public float waveProgress() {
        return Math.clamp(elapsedSeconds() / waveDurationSeconds(), 0.0F, 1.0F);
    }

    public float waveRadius() {
        float progress = waveProgress();
        float shaped = progress + (float) Math.sin(Math.PI * progress) * PROPAGATION_SWELL;
        return radius * Math.min(1.0F, shaped);
    }

    public boolean isWaveActive() {
        return isActive() && waveProgress() < 1.0F;
    }

    public int color() {
        return color;
    }

    public float elapsedSeconds() {
        return (System.nanoTime() - startTimeNanos) / 1_000_000_000.0F;
    }

    private float waveDurationSeconds() {
        return Math.max(0.05F, radius / Math.max(speed, 0.001F));
    }
}
