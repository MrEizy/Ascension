package net.zic.ascension.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
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
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.client.visual.runtime.ClientRuntimeVisuals;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client mirror of which players have a Qi Coating active: a running runtime visual with an ascension:custom element naming a
 * coating (QiCoating.fromVisual), which is how Qi Coating passives apply it. Tracked per channel (QiCoatingChannel), so a body
 * coating and a weapon coating each grow and shrink on their own clock.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class QiCoatingClientState {
    // Which coating each runtime visual definition carries, if any; cleared when the datapacks are synced again.
    private static final Map<Identifier, Optional<QiCoating>> VISUAL_COATINGS = new ConcurrentHashMap<>();
    // When each player's current coating on each channel came on, so coatings that grow (QiCoating.growMillis) know how far
    // along they are. Kept up to date by trackActivation every client tick.
    private static final Map<Key, Activation> ACTIVATIONS = new ConcurrentHashMap<>();
    // Coatings that grow and were just turned off: they stay on (get still returns them) while they shrink away, the grow-in
    // played backwards from wherever it had got to.
    private static final Map<Key, Release> RELEASES = new ConcurrentHashMap<>();

    private QiCoatingClientState() {
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
            return;
        }
        Release release = RELEASES.remove(key);
        if (activation == null || !activation.coating().equals(coating)) {
            long start = now;
            if (release != null && release.coating().equals(coating)) {
                start -= (long) (shrinking(release, now) * coating.growMillis());
            }
            ACTIVATIONS.put(key, new Activation(coating, start));
        }
    }

    /**
     * Milliseconds since the player's coating came on, starting the clock now if no tick has seen it yet.
     */
    public static long activeMillis(UUID playerId, QiCoating coating) {
        Activation activation = ACTIVATIONS.compute(new Key(playerId, channelOf(coating)), (_, current) ->
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
        Release release = RELEASES.get(new Key(playerId, channelOf(coating)));
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

    private static QiCoatingChannel channelOf(QiCoating coating) {
        return coating.swordQi() ? QiCoatingChannel.EXTERNAL : QiCoatingChannel.BODY;
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
        for (RuntimeVisualState state : ClientRuntimeVisuals.states()) {
            if (playerId.equals(state.ownerId())) {
                QiCoating coating = coatingOf(state);
                if (channel.matches(coating)) {
                    return coating;
                }
            }
        }
        return null;
    }

    /**
     * The coating with this id from the synced datapacks, or null.
     */
    public static QiCoating byId(Identifier id) {
        Minecraft minecraft = Minecraft.getInstance();
        return id == null || minecraft.level == null ? null : QiCoating.byId(id, minecraft.level.registryAccess());
    }

    private static QiCoating coatingOf(RuntimeVisualState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        if (state.definition() != null) {
            return QiCoating.fromVisual(state.definition(), minecraft.level.registryAccess());
        }
        if (state.visual() == null) {
            return null;
        }
        return VISUAL_COATINGS.computeIfAbsent(state.visual(),
                id -> Optional.ofNullable(QiCoating.fromVisual(id, minecraft.level.registryAccess()))).orElse(null);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        VISUAL_COATINGS.clear();
        ACTIVATIONS.clear();
        RELEASES.clear();
    }

    /**
     * The datapacks were synced again (joining, or /reload): coatings may have changed.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent.ClientPacketReceived event) {
        VISUAL_COATINGS.clear();
    }

    private record Key(UUID playerId, QiCoatingChannel channel) {
    }

    private record Activation(QiCoating coating, long startMillis) {
    }

    /**
     * A coating shrinking away since startMillis, from fromGrowth (how far it had grown when it went off).
     */
    private record Release(QiCoating coating, long startMillis, float fromGrowth) {
    }
}
