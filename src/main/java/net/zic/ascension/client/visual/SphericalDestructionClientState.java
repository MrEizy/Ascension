package net.zic.ascension.client.visual;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SphericalDestructionClientState {
    private static final SphericalDestructionClientState INSTANCE = new SphericalDestructionClientState();
    private static final float IMPACT_WAVE_SECONDS = 0.6F;

    public void start(Vec3 center, int radius, int i) {
    }

    private enum Phase { NONE, TRAVELING, IMPACT }

    private Phase phase = Phase.NONE;
    private long phaseStartNanos;
    private int color = 0xFF6A1B;

    // ── Travel phase state ──
    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = Vec3.ZERO;
    private double speed;
    private double startRadius;
    private double targetRadius;
    private double growthDistance;
    private double maxDistance;

    // ── Impact phase state ──
    private Vec3 impactCenter = Vec3.ZERO;
    private float impactRadius;

    private SphericalDestructionClientState() {
    }

    public static SphericalDestructionClientState get() {
        return INSTANCE;
    }

    /** Starts the client-simulated travel visual — see SphericalProjectilePayload. */
    public void launch(Vec3 origin, Vec3 direction, double speed, double startRadius,
                       double targetRadius, double growthDistance, double maxDistance, int color) {
        this.origin = origin;
        this.direction = direction.lengthSqr() > 1.0E-6 ? direction.normalize() : new Vec3(0, 0, 1);
        this.speed = Math.max(0.01, speed);
        this.startRadius = startRadius;
        this.targetRadius = targetRadius;
        this.growthDistance = Math.max(0.01, growthDistance);
        this.maxDistance = maxDistance;
        this.color = color;
        this.phase = Phase.TRAVELING;
        this.phaseStartNanos = System.nanoTime();
    }

    /** Starts the impact shockwave — see SphericalDestructionPayload. Ends travel if still active. */
    public void impact(Vec3 center, float radius, int color) {
        this.impactCenter = center;
        this.impactRadius = radius;
        this.color = color;
        this.phase = Phase.IMPACT;
        this.phaseStartNanos = System.nanoTime();
    }

    public boolean isWaveActive() {
        return switch (phase) {
            case NONE -> false;
            case TRAVELING -> traveledDistance() < maxDistance;
            case IMPACT -> impactProgress() < 1.0F;
        };
    }

    /** Current sphere center for this frame — traveling position or the fixed impact point. */
    public Vec3 center() {
        if (phase == Phase.TRAVELING) {
            double distance = Math.min(traveledDistance(), maxDistance);
            return origin.add(direction.scale(distance));
        }
        return impactCenter;
    }

    /** Current sphere radius for this frame — growing-while-traveling or the impact wave radius. */
    public float radius() {
        if (phase == Phase.TRAVELING) {
            double growth = Mth.clamp(traveledDistance() / growthDistance, 0.0, 1.0);
            return (float) Mth.lerp(growth, startRadius, targetRadius);
        }
        return impactRadius;
    }

    /** waveRadius/waveProgress feed the shader the same way regardless of phase. */
    public float waveRadius() {
        if (phase == Phase.IMPACT) {
            return impactRadius * impactProgress();
        }
        return radius();
    }

    public float waveProgress() {
        return phase == Phase.IMPACT ? impactProgress() : (float) Mth.clamp(traveledDistance() / Math.max(maxDistance, 0.01), 0.0, 1.0);
    }

    public int color() {
        return color;
    }

    public float elapsedSeconds() {
        return (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0F;
    }

    private double traveledDistance() {
        return speed * 20.0 * elapsedSeconds(); // speed is blocks/tick, 20 ticks/sec
    }

    private float impactProgress() {
        return Mth.clamp(elapsedSeconds() / IMPACT_WAVE_SECONDS, 0.0F, 1.0F);
    }
}