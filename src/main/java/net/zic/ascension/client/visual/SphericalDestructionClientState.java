package net.zic.ascension.client.visual;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SphericalDestructionClientState {
    private static final SphericalDestructionClientState INSTANCE = new SphericalDestructionClientState();
    private static final float IMPACT_SETTLE_SECONDS = 0.06F;
    private static final float IMPACT_EFFECT_SECONDS = 1.35F;
    private static final double MAX_TRAVEL_RADIUS = 2.5D;

    private enum Phase { NONE, TRAVELING, IMPACT }

    private Phase phase = Phase.NONE;
    private long phaseStartNanos;
    private int color = 0xFF6A1B;

    private Vec3 origin = Vec3.ZERO;
    private Vec3 direction = new Vec3(0, 0, 1);
    private double speed;
    private double startRadius;
    private double targetRadius;
    private double growthDistance;
    private double maxDistance;

    private Vec3 impactStartCenter = Vec3.ZERO;
    private Vec3 impactCenter = Vec3.ZERO;
    private float impactStartRadius;
    private float impactRadius;

    private SphericalDestructionClientState() {
    }

    public static SphericalDestructionClientState get() {
        return INSTANCE;
    }

    public void launch(Vec3 origin, Vec3 direction, double speed, double startRadius,
                       double targetRadius, double growthDistance, double maxDistance, int color) {
        this.direction = direction.lengthSqr() > 1.0E-6 ? direction.normalize() : new Vec3(0, 0, 1);
        this.origin = origin;
        this.speed = Math.max(0.01, speed);
        this.startRadius = Math.max(0.05, startRadius);
        this.targetRadius = Math.max(this.startRadius, targetRadius);
        this.growthDistance = Math.max(0.01, growthDistance);
        this.maxDistance = Math.max(0.01, maxDistance);
        this.color = color;
        this.phase = Phase.TRAVELING;
        this.phaseStartNanos = System.nanoTime();
    }

    public void impact(Vec3 center, float radius, int color) {
        Vec3 currentCenter = phase == Phase.TRAVELING ? center() : center;
        float currentRadius = phase == Phase.TRAVELING ? radius() : impactCoreRadius(radius);
        this.impactStartCenter = currentCenter;
        this.impactCenter = center;
        this.impactStartRadius = currentRadius;
        this.impactRadius = Math.max(radius, 0.1F);
        this.color = color;
        this.phase = Phase.IMPACT;
        this.phaseStartNanos = System.nanoTime();
    }

    public boolean isWaveActive() {
        return switch (phase) {
            case NONE -> false;
            case TRAVELING -> traveledDistance() < maxDistance;
            case IMPACT -> elapsedSeconds() < IMPACT_SETTLE_SECONDS + IMPACT_EFFECT_SECONDS;
        };
    }

    public boolean isImpactPhase() {
        return phase == Phase.IMPACT && elapsedSeconds() >= IMPACT_SETTLE_SECONDS;
    }

    public Vec3 center() {
        if (phase == Phase.TRAVELING) {
            double distance = Math.min(traveledDistance(), maxDistance);
            return origin.add(direction.scale(distance));
        }
        if (phase == Phase.IMPACT) {
            float settle = settleProgress();
            double eased = settle * settle * (3.0D - 2.0D * settle);
            return impactStartCenter.lerp(impactCenter, eased);
        }
        return impactCenter;
    }

    public float radius() {
        if (phase == Phase.TRAVELING) {
            double growth = Mth.clamp(traveledDistance() / growthDistance, 0.0, 1.0);
            growth = growth * growth * (3.0D - 2.0D * growth);
            return (float) Mth.lerp(growth, startRadius, travelTargetRadius());
        }
        if (phase == Phase.IMPACT && !isImpactPhase()) {
            float settle = settleProgress();
            double eased = settle * settle * (3.0D - 2.0D * settle);
            return (float) Mth.lerp(eased, impactStartRadius, impactCoreRadius(impactRadius));
        }
        return impactCoreRadius(impactRadius);
    }

    public float waveRadius() {
        return isImpactPhase() ? impactRadius : radius();
    }

    public float waveProgress() {
        if (phase == Phase.IMPACT) {
            return Mth.clamp((elapsedSeconds() - IMPACT_SETTLE_SECONDS) / IMPACT_EFFECT_SECONDS, 0.0F, 1.0F);
        }
        return (float) Mth.clamp(traveledDistance() / Math.max(maxDistance, 0.01), 0.0, 1.0);
    }

    public float shaderTimeSeconds() {
        float elapsed = elapsedSeconds();
        if (phase == Phase.IMPACT && elapsed >= IMPACT_SETTLE_SECONDS) {
            return elapsed - IMPACT_SETTLE_SECONDS;
        }
        return elapsed;
    }

    public Vec3 direction() {
        return direction;
    }

    public float trailLength() {
        return (float) Math.max(1.6D, radius() * 1.85D + speed * 1.5D);
    }

    public int color() {
        return color;
    }

    private double travelTargetRadius() {
        double resolved = 0.65D + Math.sqrt(Math.max(targetRadius, 1.0D)) * 0.35D;
        return Math.min(MAX_TRAVEL_RADIUS, Math.max(startRadius, resolved));
    }

    private static float impactCoreRadius(float radius) {
        return (float) Math.min(3.25D, Math.max(0.85D, Math.sqrt(Math.max(radius, 0.1F)) * 0.55D));
    }

    private double traveledDistance() {
        return speed * 20.0D * elapsedSeconds();
    }

    private float elapsedSeconds() {
        return (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0F;
    }

    private float settleProgress() {
        return Mth.clamp(elapsedSeconds() / IMPACT_SETTLE_SECONDS, 0.0F, 1.0F);
    }
}
