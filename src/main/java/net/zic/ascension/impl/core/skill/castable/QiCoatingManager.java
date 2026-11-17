package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.network.QiCoatingSyncPacket;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Which Qi Coating each entity has on each channel, server side, and keeping clients in sync with it (QiCoatingSyncPacket):
 * the entity and every player tracking it hear when a coating goes on or off, players who start tracking it get its coatings,
 * and its own player gets them again whenever its client world is rebuilt. Coatings are put on by the coating passives'
 * on_enable (ascension:qi_coating_visual for the picked element, ascension:qi_coating for a named coating), which run again
 * when the player logs in or respawns, and taken off when the passive goes off.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class QiCoatingManager {
    private static final Map<Key, Active> ACTIVE = new HashMap<>();

    private QiCoatingManager() {
    }

    /**
     * Puts a coating on the entity's channel, under the skill putting it on, replacing whatever was there.
     */
    public static void set(LivingEntity entity, QiCoatingChannel channel, Identifier coating, Identifier skill) {
        Active active = new Active(coating, entity.level().getGameTime(), skill);
        ACTIVE.put(new Key(entity.getUUID(), channel), active);
        send(entity, channel, active);
    }

    /**
     * Takes the coating off the entity's channel, if the skill put it there (another skill may have put its own on since).
     */
    public static void clear(LivingEntity entity, QiCoatingChannel channel, Identifier skill) {
        Key key = new Key(entity.getUUID(), channel);
        Active active = ACTIVE.get(key);
        if (active == null || !active.skill().equals(skill)) {
            return;
        }
        ACTIVE.remove(key);
        send(entity, channel, null);
    }

    /**
     * The coating on the entity's channel, or null.
     */
    public static @Nullable QiCoating get(LivingEntity entity, QiCoatingChannel channel) {
        Active active = ACTIVE.get(new Key(entity.getUUID(), channel));
        return active == null ? null : QiCoating.byId(active.coating(), entity.registryAccess());
    }

    private static void send(Entity entity, QiCoatingChannel channel, @Nullable Active active) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet(entity.getUUID(), channel, active));
    }

    private static QiCoatingSyncPacket packet(UUID entity, QiCoatingChannel channel, @Nullable Active active) {
        return active == null
                ? new QiCoatingSyncPacket(entity, channel, null, 0L)
                : new QiCoatingSyncPacket(entity, channel, active.coating(), active.startTick());
    }

    // Both channels, so a viewer who missed a coating going off while out of range hears that too.
    private static void sendAll(ServerPlayer viewer, UUID entity) {
        for (QiCoatingChannel channel : QiCoatingChannel.values()) {
            PacketDistributor.sendToPlayer(viewer, packet(entity, channel, ACTIVE.get(new Key(entity, channel))));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer) {
            sendAll(viewer, event.getTarget().getUUID());
        }
    }

    // Players don't track themselves, so they get their own coatings again after anything that rebuilds their client world.
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendAll(player, player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendAll(player, player.getUUID());
        }
    }

    // The passive takes it off as the player leaves; this is in case anything was missed.
    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        for (QiCoatingChannel channel : QiCoatingChannel.values()) {
            ACTIVE.remove(new Key(event.getEntity().getUUID(), channel));
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private record Key(UUID entity, QiCoatingChannel channel) {
    }

    /**
     * A coating that is on: which, since when (game ticks), and which skill put it on.
     */
    private record Active(Identifier coating, long startTick, Identifier skill) {
    }
}
