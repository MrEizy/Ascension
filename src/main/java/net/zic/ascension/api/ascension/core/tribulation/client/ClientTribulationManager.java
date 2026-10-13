package net.zic.ascension.api.ascension.core.tribulation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.network.tribulation.ClientboundTribulationAddPacket;
import net.zic.ascension.network.tribulation.ClientboundTribulationUpdatePacket;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * holds every tribulation the client is currently tracking
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class ClientTribulationManager {
    private static final Map<UUID, ClientTribulation<?>> TRIBULATIONS = new HashMap<>();
    private ClientTribulationManager() {
    }

    public static Collection<ClientTribulation<?>> getAll() {
        return Collections.unmodifiableCollection(TRIBULATIONS.values());
    }

    public static @Nullable ClientTribulation<?> get(UUID id) {
        return TRIBULATIONS.get(id);
    }

    public static void add(ClientboundTribulationAddPacket packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        Tribulation<?> tribulation = CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, packet.tribulation(), connection.registryAccess());
        if (tribulation == null) {
            AscensionCraft.LOGGER.debug("Received unknown tribulation {}", packet.tribulation());
            return;
        }
        ClientTribulation<?> clientTribulation = ClientTribulation.create(
                packet.id(),
                packet.tribulation(),
                tribulation,
                packet.dimension(),
                packet.position(),
                packet.data(),
                connection.registryAccess()
        );
        if (clientTribulation == null) return;
        remove(packet.id());
        TRIBULATIONS.put(packet.id(), clientTribulation);
    }

    public static void update(ClientboundTribulationUpdatePacket packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        ClientTribulation<?> tribulation = TRIBULATIONS.get(packet.id());
        if (tribulation == null || connection == null) return; //Note to self, should be fine, as long as ADD packet is sent properly
        tribulation.update(packet.position(), packet.data(), connection.registryAccess());
    }

    public static void remove(UUID id) {
        ClientTribulation<?> tribulation = TRIBULATIONS.remove(id);
        if (tribulation == null) return;
        tribulation.onRemoved();
    }

    public static void clear() {
        for (UUID id : java.util.List.copyOf(TRIBULATIONS.keySet())) {
            remove(id);
        }
    }

    //──Events────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;
        for (ClientTribulation<?> tribulation : TRIBULATIONS.values()) {
            if (!tribulation.getDimension().equals(minecraft.level.dimension())) continue;
            tribulation.tick();
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || TRIBULATIONS.isEmpty()) return;
        for (ClientTribulation<?> tribulation : TRIBULATIONS.values()) {
            if (!tribulation.getDimension().equals(minecraft.level.dimension())) continue;
            tribulation.render(event);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }


    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        clear();
    }
}
