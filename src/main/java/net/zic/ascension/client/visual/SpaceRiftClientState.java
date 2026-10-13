package net.zic.ascension.client.visual;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SpaceRiftClientState {
    private static final SpaceRiftClientState INSTANCE = new SpaceRiftClientState();
    // How long space collapses inward before it snaps and the glass breaks.
    public static final float COLLAPSE_SECONDS = 1.1F;
    // Fraction of the collapse after which cracks start running out of the ring.
    private static final float CRACK_START = 0.75F;
    private static final float CRACK_SECONDS = 0.45F;
    private static final float OPEN_DELAY_SECONDS = 0.3F;
    private static final float OPEN_SECONDS = 0.9F;
    private static final float CLOSE_SECONDS = 1.2F;
    private static final float FADE_SECONDS = 0.5F;
    private static final float RESIDUAL_PINCH = 0.1F;
    // Radius (in rift radii) of the void tear at the peak of the collapse.
    private static final float CORE_RADIUS = 0.2F;
    private static final int MAX_TRACKED_RIFTS = 32;
    // How far open a rift must be before it can be clicked.
    private static final float CLICKABLE_OPEN_PHASE = 0.6F;
    private static final float MIN_DURATION_SECONDS = COLLAPSE_SECONDS * CRACK_START + OPEN_DELAY_SECONDS + OPEN_SECONDS + CLOSE_SECONDS;

    private final List<Rift> rifts = new ArrayList<>();
    private Identifier currentDimension;

    public record Snapshot(Vec3 center, Vec3 right, Vec3 up, float radius, float time, float seed,
                           float crackPhase, float openPhase, float fade,
                           float pinch, float coreRadius, float glow) {
        /** Whether the scene behind the rift needs warping this frame. */
        public boolean distorts() {
            return Math.abs(pinch) > 0.002F || coreRadius > 0.0F || glow > 0.01F;
        }
    }

    private SpaceRiftClientState() {
    }

    public static SpaceRiftClientState get() {
        return INSTANCE;
    }

    public void clear() {
        rifts.clear();
        currentDimension = null;
    }

    /**
     * Opens a rift on the plane through {@code center} that faces {@code facing}. The rift's vertical
     * axis is aligned as closely as possible with {@code upHint} (keeps the shard pattern upright).
     */
    public void open(Identifier dimension, Vec3 center, Vec3 facing, Vec3 upHint,
                     float radius, float durationSeconds) {
        open(UUID.randomUUID(), dimension, center, facing, upHint, radius, durationSeconds, false);
    }

    /**
     * Same as {@link #open}, but with an id so the server can {@link #close} it later. Re-opening a known id is ignored.
     * An {@code interactable} rift can be clicked once open (see {@link #pickInteractable}).
     */
    public void open(UUID id, Identifier dimension, Vec3 center, Vec3 facing, Vec3 upHint,
                     float radius, float durationSeconds, boolean interactable) {
        syncDimension(dimension);
        for (Rift rift : rifts) {
            if (rift.id.equals(id)) {
                return;
            }
        }
        if (rifts.size() >= MAX_TRACKED_RIFTS) {
            rifts.removeFirst();
        }
        Vec3 normal = facing.lengthSqr() > 1.0E-6D ? facing.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 reference = Math.abs(normal.dot(upHint.normalize())) > 0.95D
                ? (Math.abs(normal.y) > 0.95D ? new Vec3(0.0D, 0.0D, 1.0D) : new Vec3(0.0D, 1.0D, 0.0D))
                : upHint;
        Vec3 right = reference.cross(normal).normalize();
        Vec3 up = normal.cross(right).normalize();
        float duration = Math.max(durationSeconds, MIN_DURATION_SECONDS);
        float seed = (float) (Math.random() * 97.0D);
        rifts.add(new Rift(id, center, right, up, Math.max(radius, 0.25F), duration, seed, System.nanoTime(), interactable));
    }

    public record Pick(UUID id, double distance) {
    }

    /**
     * Finds the nearest interactable rift whose open hole the ray passes through, ignoring rifts that
     * are still collapsing, barely open, or already closing.
     */
    public Pick pickInteractable(Vec3 origin, Vec3 direction, double maxDistance) {
        long now = System.nanoTime();
        Pick best = null;
        for (Rift rift : rifts) {
            if (!rift.interactable) {
                continue;
            }
            float age = (now - rift.createdNanos) / 1.0E9F;
            if (age >= rift.duration) {
                continue;
            }
            Snapshot snapshot = rift.snapshot(age);
            if (snapshot.openPhase() < CLICKABLE_OPEN_PHASE) {
                continue;
            }
            Vec3 normal = rift.right.cross(rift.up);
            double denom = direction.dot(normal);
            if (Math.abs(denom) < 1.0E-4D) {
                continue;
            }
            double distance = rift.center.subtract(origin).dot(normal) / denom;
            if (distance < 0.0D || distance > maxDistance || (best != null && distance >= best.distance)) {
                continue;
            }
            Vec3 offset = origin.add(direction.scale(distance)).subtract(rift.center);
            double localX = offset.dot(rift.right) / rift.radius;
            double localY = offset.dot(rift.up) / rift.radius;
            // The hole edge sits at roughly 0.95 * openPhase rift radii; stay a little inside it.
            if (localX * localX + localY * localY <= Math.pow(0.9D * snapshot.openPhase(), 2.0D)) {
                best = new Pick(rift.id, distance);
            }
        }
        return best;
    }

    /** Starts closing the rift now (it still finishes opening first if it is very young). */
    public void close(UUID id) {
        long now = System.nanoTime();
        for (int index = 0; index < rifts.size(); index++) {
            Rift rift = rifts.get(index);
            if (rift.id.equals(id)) {
                float age = (now - rift.createdNanos) / 1.0E9F;
                float duration = Math.max(age + CLOSE_SECONDS, MIN_DURATION_SECONDS);
                if (duration < rift.duration) {
                    rifts.set(index, rift.withDuration(duration));
                }
                return;
            }
        }
    }

    public List<Snapshot> snapshots(Identifier dimension) {
        syncDimension(dimension);
        long now = System.nanoTime();
        List<Snapshot> snapshots = new ArrayList<>(rifts.size());
        for (Iterator<Rift> iterator = rifts.iterator(); iterator.hasNext(); ) {
            Rift rift = iterator.next();
            float age = (now - rift.createdNanos) / 1.0E9F;
            if (age >= rift.duration) {
                iterator.remove();
                continue;
            }
            snapshots.add(rift.snapshot(age));
        }
        return snapshots;
    }

    private void syncDimension(Identifier dimension) {
        if (currentDimension != null && !currentDimension.equals(dimension)) {
            rifts.clear();
        }
        currentDimension = dimension;
    }

    private record Rift(UUID id, Vec3 center, Vec3 right, Vec3 up, float radius, float duration, float seed,
                        long createdNanos, boolean interactable) {

        private Rift withDuration(float newDuration) {
            return new Rift(id, center, right, up, radius, newDuration, seed, createdNanos, interactable);
        }

        private Snapshot snapshot(float age) {
            float remaining = duration - age;
            float sinceSnap = age - COLLAPSE_SECONDS;
            float closing = Mth.clamp(remaining / CLOSE_SECONDS, 0.0F, 1.0F);
            float closingEase = closing * closing * (3.0F - 2.0F * closing);
            float fade = Mth.clamp(remaining / FADE_SECONDS, 0.0F, 1.0F);

            // Collapse: space is dragged inward ever harder around a growing tear of void. At the snap the
            // pull eases back down to a faint permanent lensing instead of springing back.
            float collapse = Mth.clamp(age / COLLAPSE_SECONDS, 0.0F, 1.0F);
            float pinch = sinceSnap < 0.0F
                    ? (float) Math.pow(collapse, 2.2D)
                    : RESIDUAL_PINCH * closingEase + (1.0F - RESIDUAL_PINCH) * (float) Math.exp(-sinceSnap * 3.0D);
            pinch *= fade;

            // The void tear at the centre grows during the collapse and becomes the start of the hole.
            float core = CORE_RADIUS * (sinceSnap < 0.0F ? (float) Math.pow(collapse, 1.5D) : 1.0F) * closingEase;

            // Event-horizon glow: builds through the collapse, peaks at the snap, then fades out.
            float glow = sinceSnap < 0.0F
                    ? Math.max(0.6F * collapse, (float) Math.exp(sinceSnap * 5.0D))
                    : (float) Math.exp(-sinceSnap * 4.0D);
            glow *= fade;

            // Cracks start running out of the ring just before the snap; the glass opens right as it hits.
            float crackAge = age - COLLAPSE_SECONDS * CRACK_START;
            float crack = easeOut(Mth.clamp(crackAge / CRACK_SECONDS, 0.0F, 1.0F));
            float open = easeOut(Mth.clamp((crackAge - OPEN_DELAY_SECONDS) / OPEN_SECONDS, 0.0F, 1.0F));
            open *= closingEase;
            crack *= Mth.clamp(remaining / (CLOSE_SECONDS * 0.6F), 0.0F, 1.0F);
            return new Snapshot(center, right, up, radius, age, seed, crack, open, fade, pinch, core, glow);
        }

        private static float easeOut(float value) {
            float inverse = 1.0F - value;
            return 1.0F - inverse * inverse * inverse;
        }
    }
}
