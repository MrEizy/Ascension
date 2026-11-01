package net.zic.ascension.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client mirror of which players have a Qi Coating on, as the server tells it (QiCoatingManager, QiCoatingSyncPacket). Tracked
 * per channel (QiCoatingChannel), so a body coating and a weapon coating each grow and shrink on their own clock; the clock
 * starts when the server put the coating on, so a player coming into view mid-way shows it as far grown as everyone else sees.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class QiCoatingClientState {
    // When each player's current coating on each channel came on, so coatings that grow (QiCoating.growMillis) know how far
    // along they are. Kept up to date by trackActivation every client tick.
    private static final Map<Key, Activation> ACTIVATIONS = new HashMap<>();
    // Coatings that grow and were just turned off: they stay on (get still returns them) while they shrink away, the grow-in
    // played backwards from wherever it had got to.
    private static final Map<Key, Release> RELEASES = new HashMap<>();
    // The coating each player has on each channel right now (live), and when it came on, as the server last said.
    private static final Map<Key, Live> LIVE = new HashMap<>();
    // The biome shade each player's foliage coating shows on each channel (foliage), eased towards the biome they stand in.
    private static final Map<Key, Foliage> FOLIAGE = new HashMap<>();

    private QiCoatingClientState() {
    }

    /**
     * The server says what coating the entity now has on the channel (null for none), and the game tick it went on.
     */
    public static void accept(UUID entity, QiCoatingChannel channel, @Nullable Identifier coatingId, long startTick) {
        Minecraft minecraft = Minecraft.getInstance();
        QiCoating coating = coatingId == null || minecraft.level == null ? null : QiCoating.byId(coatingId, minecraft.level.registryAccess());
        Key key = new Key(entity, channel);
        if (coating == null) {
            LIVE.remove(key);
            return;
        }
        long ticksAgo = Math.max(0L, minecraft.level.getGameTime() - startTick);
        LIVE.put(key, new Live(coating, Util.getMillis() - ticksAgo * 50L));
    }

    /**
     * Records when the player's coating on a channel changed: a new coating restarts the clock, and none starts a coating that
     * grows shrinking away. Turning the same coating back on while it shrinks makes it grow again from where it got to.
     *
     * @param coating the player's live coating on the channel ({@link #live}), not counting one still shrinking away
     */
    public static void trackActivation(UUID playerId, QiCoatingChannel channel, QiCoating coating) {
        Key key = new Key(playerId, channel);
        long now = Util.getMillis();
        Activation activation = ACTIVATIONS.get(key);
        if (coating == null) {
            ACTIVATIONS.remove(key);
            if (activation != null && activation.coating().grows()) {
                RELEASES.put(key, new Release(activation.coating(), now, growth(activation, now)));
            } else {
                Release release = RELEASES.get(key);
                if (release != null && shrinking(release, now) <= 0.0F) {
                    RELEASES.remove(key);
                }
            }
            // Kept while the coating still shrinks away in its shade.
            if (!RELEASES.containsKey(key)) {
                FOLIAGE.remove(key);
            }
            return;
        }
        Release release = RELEASES.remove(key);
        if (activation == null || !activation.coating().equals(coating)) {
            // When the server put it on, or, turned back on while still shrinking away, grown again from where it got to.
            Live live = LIVE.get(key);
            long start = live != null && live.coating().equals(coating) ? live.startMillis() : now;
            if (release != null && release.coating().equals(coating)) {
                start = now - (long) (shrinking(release, now) * coating.growMillis());
            }
            ACTIVATIONS.put(key, new Activation(coating, start));
        }
    }

    /**
     * Milliseconds since the player's coating came on, starting the clock now if no tick has seen it yet.
     */
    public static long activeMillis(UUID playerId, QiCoating coating) {
        Activation activation = ACTIVATIONS.compute(new Key(playerId, QiCoatingChannel.of(coating)), (_, current) ->
                current == null || !current.coating().equals(coating) ? new Activation(coating, Util.getMillis()) : current);
        return Util.getMillis() - activation.startMillis();
    }

    /**
     * How far the player's coating has grown, 0 to 1: rising after it comes on, falling again while it shrinks away after it
     * goes off. Always 1 for coatings that don't grow.
     */
    public static float growth(UUID playerId, QiCoating coating) {
        if (!coating.grows()) {
            return 1.0F;
        }
        long now = Util.getMillis();
        Release release = RELEASES.get(new Key(playerId, QiCoatingChannel.of(coating)));
        if (release != null && release.coating().equals(coating)) {
            return shrinking(release, now);
        }
        // Starts the clock if no tick has seen this coating yet (or ever will, for an entity that isn't a player).
        return Mth.clamp((float) activeMillis(playerId, coating) / coating.growMillis(), 0.0F, 1.0F);
    }

    private static float growth(Activation activation, long now) {
        return Mth.clamp((float) (now - activation.startMillis()) / activation.coating().growMillis(), 0.0F, 1.0F);
    }

    private static float shrinking(Release release, long now) {
        return Mth.clamp(release.fromGrowth() - (float) (now - release.startMillis()) / release.coating().growMillis(), 0.0F, 1.0F);
    }

    /**
     * The biome foliage colour a foliage coating shows (QiCoatingDefinition foliageMillis, foliageLocked), given the colour of the
     * biome the player stands in now: the shade it came on in, then eased towards the current biome over foliage_millis
     * (straight to it for 0), or kept for good if it's locked. Turning the coating on again starts from the biome again.
     */
    public static int foliage(UUID playerId, QiCoating coating, int biomeColor) {
        QiCoatingDefinition definition = coating.definition();
        if (definition.foliageMillis() <= 0 && !definition.foliageLocked()) {
            return biomeColor;
        }
        Key key = new Key(playerId, QiCoatingChannel.of(coating));
        long now = Util.getMillis();
        // A coating still shrinking away has no activation; it keeps the shade it had.
        Activation activation = ACTIVATIONS.get(key);
        long start = activation == null ? Long.MIN_VALUE : activation.startMillis();
        Foliage foliage = FOLIAGE.get(key);
        if (foliage == null || !foliage.coating.equals(coating) || activation != null && foliage.startMillis != start) {
            foliage = new Foliage(coating, start, biomeColor, now);
            FOLIAGE.put(key, foliage);
            return biomeColor;
        }
        if (definition.foliageLocked()) {
            return foliage.color();
        }
        // Eased so it gets about 95% of the way in foliage_millis, however often it's drawn.
        float step = 1.0F - (float) Math.exp(-3.0D * (now - foliage.lastMillis) / definition.foliageMillis());
        foliage.lastMillis = now;
        foliage.red += (ARGB.red(biomeColor) - foliage.red) * step;
        foliage.green += (ARGB.green(biomeColor) - foliage.green) * step;
        foliage.blue += (ARGB.blue(biomeColor) - foliage.blue) * step;
        return foliage.color();
    }

    /**
     * The coating the player shows on a channel: their live one, or else one still shrinking away after it went off.
     */
    public static QiCoating get(UUID playerId, QiCoatingChannel channel) {
        if (playerId == null) {
            return null;
        }
        QiCoating coating = live(playerId, channel);
        if (coating != null) {
            return coating;
        }
        Release release = RELEASES.get(new Key(playerId, channel));
        return release != null && shrinking(release, Util.getMillis()) > 0.0F ? release.coating() : null;
    }

    /**
     * The coating the player has on a channel right now, not counting one still shrinking away.
     */
    public static QiCoating live(UUID playerId, QiCoatingChannel channel) {
        if (playerId == null) {
            return null;
        }
        Live live = LIVE.get(new Key(playerId, channel));
        return live == null ? null : live.coating();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LIVE.clear();
        ACTIVATIONS.clear();
        RELEASES.clear();
        FOLIAGE.clear();
    }

    /**
     * The datapacks were synced again (joining, or /reload): the coatings on are looked up again, as their definitions may
     * have changed.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent.ClientPacketReceived event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        LIVE.replaceAll((key, live) -> {
            QiCoating coating = QiCoating.byId(live.coating().id(), minecraft.level.registryAccess());
            return coating == null ? live : new Live(coating, live.startMillis());
        });
    }

    private record Key(UUID playerId, QiCoatingChannel channel) {
    }

    private record Live(QiCoating coating, long startMillis) {
    }

    private record Activation(QiCoating coating, long startMillis) {
    }

    /**
     * A foliage coating's shade: which coating it is, the activation it came on with, its colour as floats (eased a little
     * each frame) and when it was last eased.
     */
    private static final class Foliage {
        private final QiCoating coating;
        private final long startMillis;
        private float red;
        private float green;
        private float blue;
        private long lastMillis;

        private Foliage(QiCoating coating, long startMillis, int color, long now) {
            this.coating = coating;
            this.startMillis = startMillis;
            this.red = ARGB.red(color);
            this.green = ARGB.green(color);
            this.blue = ARGB.blue(color);
            this.lastMillis = now;
        }

        private int color() {
            return ARGB.color(255, Math.round(red), Math.round(green), Math.round(blue));
        }
    }

    /**
     * A coating shrinking away since startMillis, from fromGrowth (how far it had grown when it went off).
     */
    private record Release(QiCoating coating, long startMillis, float fromGrowth) {
    }
}
