package net.zic.ascension.client.visual;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SphericalDestructionClientState {
    private static final SphericalDestructionClientState INSTANCE = new SphericalDestructionClientState();
    private static final float IMPACT_EFFECT_SECONDS = 1.35F;
    private static final float IMPACT_CORRECTION_SECONDS = 0.06F;
    private static final double MAX_TRAVEL_RADIUS = 2.5D;
    private static final int MAX_TRACKED_EFFECTS = 128;

    private final Map<UUID, Effect> effects = new LinkedHashMap<>();
    private Identifier currentDimension;

    private enum Phase { TRAVELING, IMPACT }

    public record Snapshot(UUID id, Vec3 center, Vec3 direction, float radius, float progress,
                           float animationTime, float trailLength, float impactCoreRadius,
                           int color, boolean impact) {
    }

    private SphericalDestructionClientState() {
    }

    public static SphericalDestructionClientState get() {
        return INSTANCE;
    }

    public void clear() {
        effects.clear();
        currentDimension = null;
    }

    public void launch(UUID id, Identifier dimension, Vec3 origin, Vec3 direction, double speed,
                       double startRadius, double targetRadius, double growthDistance,
                       double maxDistance, double initialTraveled, int color) {
        syncDimension(dimension);
        if (effects.containsKey(id)) {
            return;
        }
        if (effects.size() >= MAX_TRACKED_EFFECTS) {
            effects.remove(effects.keySet().iterator().next());
        }
        effects.put(id, new Effect(origin, direction, speed, startRadius, targetRadius,
                growthDistance, maxDistance, initialTraveled, color));
    }

    public void impact(UUID id, Identifier dimension, Vec3 center, float radius, float travelRadius,
                       int ageTicks, int color) {
        syncDimension(dimension);
        Effect effect = effects.get(id);
        if (effect == null) {
            if (effects.size() >= MAX_TRACKED_EFFECTS) {
                effects.remove(effects.keySet().iterator().next());
            }
            effect = new Effect(center, Vec3.ZERO, 0.05D, travelRadius, radius,
                    1.0D, 1.0D, 0.0D, color);
            effects.put(id, effect);
        }
        effect.impact(center, radius, travelRadius, ageTicks, color);
    }

    public List<Snapshot> snapshots(Identifier dimension) {
        syncDimension(dimension);
        long now = System.nanoTime();
        List<Snapshot> snapshots = new ArrayList<>(effects.size());
        for (Iterator<Map.Entry<UUID, Effect>> iterator = effects.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<UUID, Effect> entry = iterator.next();
            Effect effect = entry.getValue();
            if (!effect.active(now)) {
                iterator.remove();
                continue;
            }
            snapshots.add(effect.snapshot(entry.getKey(), now));
        }
        return snapshots;
    }

    private void syncDimension(Identifier dimension) {
        if (currentDimension != null && !currentDimension.equals(dimension)) {
            effects.clear();
        }
        currentDimension = dimension;
    }

    private static final class Effect {
        private final Vec3 origin;
        private final Vec3 direction;
        private final boolean impactOnly;
        private final double speed;
        private final double startRadius;
        private final double targetRadius;
        private final double growthDistance;
        private final double maxDistance;
        private final double initialTraveled;
        private final long launchReceivedNanos;
        private final float initialLaunchAgeSeconds;
        private Phase phase = Phase.TRAVELING;
        private int color;
        private Vec3 impactFrom = Vec3.ZERO;
        private Vec3 impactAt = Vec3.ZERO;
        private float impactFromRadius;
        private float impactRadius;
        private long impactReceivedNanos;
        private float initialImpactAgeSeconds;

        private Effect(Vec3 origin, Vec3 direction, double speed, double startRadius,
                       double targetRadius, double growthDistance, double maxDistance,
                       double initialTraveled, int color) {
            this.origin = origin;
            this.impactOnly = direction.lengthSqr() < 1.0E-6D;
            this.direction = direction.lengthSqr() > 1.0E-6 ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
            this.speed = Math.max(speed, 0.01D);
            this.startRadius = Math.max(startRadius, 0.05D);
            this.targetRadius = Math.max(targetRadius, this.startRadius);
            this.growthDistance = Math.max(growthDistance, 0.01D);
            this.maxDistance = Math.max(maxDistance, 0.01D);
            this.initialTraveled = Mth.clamp(initialTraveled, 0.0D, this.maxDistance);
            this.initialLaunchAgeSeconds = (float) (this.initialTraveled / (this.speed * 20.0D));
            this.launchReceivedNanos = System.nanoTime();
            this.color = color;
        }

        private void impact(Vec3 center, float radius, float serverTravelRadius, int ageTicks, int color) {
            if (phase == Phase.IMPACT) {
                return;
            }
            long now = System.nanoTime();
            this.impactFrom = travelCenter(now);
            this.impactFromRadius = travelRadius(now);
            if (impactOnly) {
                this.impactFrom = center;
                this.impactFromRadius = Math.max(serverTravelRadius, 0.05F);
            }
            this.impactAt = center;
            this.impactRadius = Math.max(radius, 0.1F);
            this.color = color;
            this.phase = Phase.IMPACT;
            this.initialImpactAgeSeconds = Math.max(0, ageTicks) / 20.0F;
            this.impactReceivedNanos = now;
        }

        private boolean active(long now) {
            if (phase == Phase.IMPACT) {
                return impactAge(now) < IMPACT_EFFECT_SECONDS;
            }
            return (now - launchReceivedNanos) / 1.0E9D <
                    Math.max(0.0D, (maxDistance - initialTraveled) / (speed * 20.0D)) + 2.0D;
        }

        private Snapshot snapshot(UUID id, long now) {
            float animationTime = initialLaunchAgeSeconds + (now - launchReceivedNanos) / 1.0E9F;
            if (phase == Phase.TRAVELING) {
                Vec3 center = travelCenter(now);
                float radius = travelRadius(now);
                float progress = (float) Mth.clamp(traveledDistance(now) / maxDistance, 0.0D, 1.0D);
                float trailLength = (float) Math.max(1.6D, radius * 1.85D + speed * 1.5D);
                return new Snapshot(id, center, direction, radius, progress, animationTime,
                        trailLength, radius, color, false);
            }
            float seconds = impactAge(now);
            float progress = Mth.clamp(seconds / IMPACT_EFFECT_SECONDS, 0.0F, 1.0F);
            float settle = Mth.clamp(seconds / IMPACT_CORRECTION_SECONDS, 0.0F, 1.0F);
            float settleEase = settle * settle * (3.0F - 2.0F * settle);
            Vec3 center = impactFrom.lerp(impactAt, settleEase);
            float charge = Mth.clamp(progress / 0.12F, 0.0F, 1.0F);
            float chargeRadius = impactFromRadius * (1.0F - 0.035F * Mth.sin(charge * (float) Math.PI));
            float grow = Mth.clamp((progress - 0.12F) / 0.38F, 0.0F, 1.0F);
            float growEase = 1.0F - (float) Math.pow(1.0F - grow, 3.0F);
            float radius = Mth.lerp(growEase, chargeRadius, impactRadius);
            float aftershock = Mth.clamp((progress - 0.55F) / 0.35F, 0.0F, 1.0F);
            radius *= 1.0F + aftershock * 0.07F;
            return new Snapshot(id, center, direction, radius, progress, animationTime,
                    0.0F, impactFromRadius, color, true);
        }

        private double traveledDistance(long now) {
            return Math.min(maxDistance, initialTraveled + (now - launchReceivedNanos) / 1.0E9D * speed * 20.0D);
        }

        private Vec3 travelCenter(long now) {
            return origin.add(direction.scale(traveledDistance(now)));
        }

        private float travelRadius(long now) {
            double growth = Mth.clamp(traveledDistance(now) / growthDistance, 0.0D, 1.0D);
            growth = growth * growth * (3.0D - 2.0D * growth);
            double resolved = 0.65D + Math.sqrt(Math.max(targetRadius, 1.0D)) * 0.35D;
            double cap = Math.min(MAX_TRAVEL_RADIUS, Math.max(startRadius, resolved));
            return (float) Mth.lerp(growth, startRadius, cap);
        }

        private float impactAge(long now) {
            return initialImpactAgeSeconds + (now - impactReceivedNanos) / 1.0E9F;
        }
    }
}
