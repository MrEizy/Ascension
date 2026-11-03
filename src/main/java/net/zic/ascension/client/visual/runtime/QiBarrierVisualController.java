package net.zic.ascension.client.visual.runtime;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.client.renderer.QiParticle;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Qi Barrier (skills/castable/qi_barrier): a film of plain Pure Qi over the player's whole body, drawn by QiCoatingLayer
 * (submitBarrierFilm, and on the arm in first person) with qi_coating.fsh style 59, from what this works out (film). It fades in
 * as qi particles are drawn in to the body from round it; flashes when a hit takes from it (the synced durability drops);
 * flashes out as particles burst off the body when it's broken; and fades as particles drift off when it runs out. Broken and
 * run out are told apart by when it goes: a barrier the server removes before its time was broken. The particles are drawn here,
 * with our own shaders after a shader pack (QiParticle).
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class QiBarrierVisualController implements RuntimeVisualController {
    public static final Identifier ID = AscensionCraft.prefix("qi_barrier");

    // The oval round the body the particles land on and leave from: its radius and half height (blocks), round the body's
    // middle.
    private static final float BODY_RADIUS = 0.4F;
    private static final float BODY_HALF_HEIGHT = 1.0F;
    // Forming: how long it takes (as the Pure Qi coating's grow) and the share of it before the film starts fading in; the
    // particles drawn in and how far out they start (blocks); when they land is QiParticle's (arrival, departure).
    private static final long FORM_MILLIS = 1800L;
    private static final float FADE_FROM = 0.3F;
    private static final int FORM_SPECKS = 40;
    private static final float FORM_START_MIN = 0.5F;
    private static final float FORM_START_MAX = 1.1F;
    // A hit's flash.
    private static final long FLASH_MILLIS = 250L;
    // Breaking: the film flashes out over BURST_FLASH_MILLIS as BURST_SPECKS particles burst BURST_DRIFT blocks off the body over
    // BURST_MILLIS.
    private static final long BURST_FLASH_MILLIS = 200L;
    private static final int BURST_SPECKS = 60;
    private static final float BURST_DRIFT = 1.2F;
    private static final long BURST_MILLIS = 900L;
    // Running out: the film fades over DISSOLVE_MILLIS as DISSOLVE_SPECKS particles drift DISSOLVE_DRIFT blocks off it over
    // DRIFT_MILLIS.
    private static final long DISSOLVE_MILLIS = 700L;
    private static final int DISSOLVE_SPECKS = 30;
    private static final float DISSOLVE_DRIFT = 0.5F;
    private static final long DRIFT_MILLIS = 1200L;
    // The particles' colours (the Pure Qi coating's gather), size (half, blocks) and glow.
    private static final int[] SPECK_COLORS = {0xFFBFF4FF, 0xFF8FE3F5, 0xFFE6FBFF};
    private static final float SPECK_HALF = 0.025F;
    private static final float SPECK_GLOW = 0.1F;
    // Where the body's middle is taken to be when its player isn't in sight: this far up from where the server last put it.
    private static final double FALLBACK_MIDDLE = 0.9D;
    // A barrier the server removes this many ticks or more before it was due to run out was broken.
    private static final long EARLY_TICKS = 2L;

    private static final Map<UUID, Live> LIVE = new HashMap<>();
    private static final List<Ending> ENDINGS = new ArrayList<>();

    public static void registerDefault() {
        ClientRuntimeVisuals.register(ID, new QiBarrierVisualController());
    }

    /**
     * How the film over a player's body looks now, or null if they have no barrier (nor one just gone).
     *
     * @param color the film's vertex colour for qi_coating.fsh style 59: alpha how faded in it is, blue down from 1 by the flash
     */
    public record Film(int color) {
        private static Film of(float alpha, float flash) {
            return new Film(ARGB.color(
                    Math.round(Math.clamp(alpha, 0.0F, 1.0F) * 255.0F),
                    255,
                    255,
                    Math.round((1.0F - Math.clamp(flash, 0.0F, 1.0F)) * 255.0F)));
        }
    }

    public static Film film(UUID playerId) {
        long now = Util.getMillis();
        for (Live live : LIVE.values()) {
            if (playerId.equals(live.ownerId)) {
                float growth = Math.clamp((now - live.spawnMillis) / (float) FORM_MILLIS, 0.0F, 1.0F);
                float faded = Math.clamp((growth - FADE_FROM) / (1.0F - FADE_FROM), 0.0F, 1.0F);
                float flash = Math.clamp(1.0F - (now - live.flashMillis) / (float) FLASH_MILLIS, 0.0F, 1.0F);
                return faded > 0.0F ? Film.of(faded, flash) : null;
            }
        }
        for (Ending ending : ENDINGS) {
            if (!playerId.equals(ending.ownerId)) {
                continue;
            }
            long age = now - ending.startMillis;
            float left = 1.0F - Math.clamp(age / (float) (ending.broken ? BURST_FLASH_MILLIS : DISSOLVE_MILLIS), 0.0F, 1.0F);
            if (left > 0.0F) {
                // A breaking film flashes as it goes; one running out just fades.
                return Film.of(left, ending.broken ? left : 0.0F);
            }
        }
        return null;
    }

    @Override
    public void onSpawn(RuntimeVisualState state) {
        LIVE.put(state.runtimeId(), new Live(state.ownerId(), Util.getMillis(), state.progress()));
    }

    @Override
    public void onUpdate(RuntimeVisualState previous, RuntimeVisualState current) {
        Live live = LIVE.computeIfAbsent(current.runtimeId(), _ -> new Live(current.ownerId(), Util.getMillis(), current.progress()));
        if (current.progress() < live.progress - 1.0E-4F) {
            live.flashMillis = Util.getMillis();
        }
        live.progress = current.progress();
    }

    @Override
    public void onRemove(RuntimeVisualState state) {
        LIVE.remove(state.runtimeId());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        boolean broken = state.expiresAt() > 0L && minecraft.level.getGameTime() < state.expiresAt() - EARLY_TICKS;
        ENDINGS.add(new Ending(state.ownerId(), broken, Util.getMillis(), middle(state, ClientRuntimeVisuals.partialTick()), state.seed()));
    }

    /**
     * The particles drawn in to the body while the barrier forms.
     */
    @Override
    public void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        Live live = LIVE.get(state.runtimeId());
        if (minecraft.level == null || live == null) {
            return;
        }
        float growth = Math.clamp((Util.getMillis() - live.spawnMillis) / (float) FORM_MILLIS, 0.0F, 1.0F);
        if (growth >= 1.0F) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Vector3f middle = middle(state, ClientRuntimeVisuals.partialTick()).subtract(camera).toVector3f();
        QiParticle.drawOrDefer(event.getPoseStack(), formingSpecks(middle, growth, state.seed()), SPECK_GLOW);
    }

    /**
     * The particles leaving barriers already gone, which outlive their runtime visual.
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ENDINGS.clear();
            return;
        }
        if (ENDINGS.isEmpty()) {
            return;
        }
        long now = Util.getMillis();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        PoseStack poseStack = event.getPoseStack();
        List<QiParticle.Speck> specks = new ArrayList<>();
        Iterator<Ending> iterator = ENDINGS.iterator();
        while (iterator.hasNext()) {
            Ending ending = iterator.next();
            long age = now - ending.startMillis;
            if (age > (ending.broken ? BURST_MILLIS : DRIFT_MILLIS)) {
                iterator.remove();
                continue;
            }
            Vector3f middle = ending.middle.subtract(camera).toVector3f();
            if (ending.broken) {
                leavingSpecks(middle, ending.seed, age / (float) BURST_MILLIS, BURST_SPECKS, BURST_DRIFT, specks);
            } else {
                leavingSpecks(middle, ending.seed, age / (float) DRIFT_MILLIS, DISSOLVE_SPECKS, DISSOLVE_DRIFT, specks);
            }
        }
        QiParticle.drawOrDefer(poseStack, specks, SPECK_GLOW);
    }

    /**
     * The particles drawn in as the film forms: each from a fixed point a little way out to a point round the body, landing one
     * after another, slowly at first and quickening as they close in (as the coatings' gather).
     */
    private static List<QiParticle.Speck> formingSpecks(Vector3f middle, float growth, long seed) {
        List<QiParticle.Speck> specks = new ArrayList<>();
        RandomSource random = RandomSource.create(seed);
        for (int i = 0; i < FORM_SPECKS; i++) {
            Vector3f direction = randomDirection(random);
            float distance = Mth.lerp(random.nextFloat(), FORM_START_MIN, FORM_START_MAX);
            float arrive = QiParticle.arrival(i, FORM_SPECKS);
            float depart = QiParticle.departure(arrive, random.nextFloat());
            int color = SPECK_COLORS[random.nextInt(SPECK_COLORS.length)];
            float half = SPECK_HALF * QiParticle.sizeScale(random.nextFloat());
            if (growth < depart || growth > arrive) {
                continue;
            }
            float t = (growth - depart) / (arrive - depart);
            Vector3f at = roundBody(middle, direction, distance * (1.0F - QiParticle.gather(t)));
            specks.add(new QiParticle.Speck(at.x, at.y, at.z, half * (1.0F - 0.5F * t), color, Math.min(1.0F, t / 0.2F)));
        }
        return specks;
    }

    /**
     * The particles leaving a barrier that's gone, `t` (0 to 1) into their flight: each from a point round the body out by up
     * to `drift` blocks, fast at first and slowing, fading as it goes.
     */
    private static void leavingSpecks(Vector3f middle, long seed, float t, int count, float drift, List<QiParticle.Speck> specks) {
        if (t >= 1.0F) {
            return;
        }
        RandomSource random = RandomSource.create(seed);
        for (int i = 0; i < count; i++) {
            Vector3f direction = randomDirection(random);
            float reach = drift * (0.6F + random.nextFloat() * 0.4F);
            int color = SPECK_COLORS[random.nextInt(SPECK_COLORS.length)];
            float half = SPECK_HALF * QiParticle.sizeScale(random.nextFloat());
            Vector3f at = roundBody(middle, direction, reach * QiParticle.scatter(t));
            specks.add(new QiParticle.Speck(at.x, at.y, at.z, half * (1.0F - 0.5F * t), color, 1.0F - t));
        }
    }

    // A point `out` blocks beyond the oval round the body in `direction` (a unit vector) from its middle.
    private static Vector3f roundBody(Vector3f middle, Vector3f direction, float out) {
        return new Vector3f(
                middle.x + direction.x * (BODY_RADIUS + out),
                middle.y + direction.y * (BODY_HALF_HEIGHT + out),
                middle.z + direction.z * (BODY_RADIUS + out));
    }

    private static Vector3f randomDirection(RandomSource random) {
        float y = random.nextFloat() * 2.0F - 1.0F;
        float angle = random.nextFloat() * Mth.TWO_PI;
        float ring = Mth.sqrt(1.0F - y * y);
        return new Vector3f(Mth.cos(angle) * ring, y, Mth.sin(angle) * ring);
    }

    // The middle of the barrier's player, following them smoothly, or a little up from where the server last put it.
    private static Vec3 middle(RuntimeVisualState state, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player owner = state.ownerId() == null || minecraft.level == null ? null : minecraft.level.getPlayerByUUID(state.ownerId());
        if (owner == null) {
            return state.position().add(0.0D, FALLBACK_MIDDLE, 0.0D);
        }
        return ClientRuntimeVisuals.interpolatedPosition(owner, partialTick).add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);
    }

    private static final class Live {
        private final UUID ownerId;
        private final long spawnMillis;
        private long flashMillis = Long.MIN_VALUE / 2;
        private float progress;

        private Live(UUID ownerId, long spawnMillis, float progress) {
            this.ownerId = ownerId;
            this.spawnMillis = spawnMillis;
            this.progress = progress;
        }
    }

    private record Ending(UUID ownerId, boolean broken, long startMillis, Vec3 middle, long seed) {
    }
}
