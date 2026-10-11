package net.zic.ascension.client.tribulation;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
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
    private static final Map<UUID, ClientTribulation> TRIBULATIONS = new HashMap<>();

    private ClientTribulationManager() {
    }

    public static Collection<ClientTribulation> getAll() {
        return Collections.unmodifiableCollection(TRIBULATIONS.values());
    }

    public static @Nullable ClientTribulation get(UUID id) {
        return TRIBULATIONS.get(id);
    }

    public static void add(ClientboundTribulationAddPacket packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        Tribulation tribulation = CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, packet.tribulation(), connection.registryAccess());
        if (tribulation == null) {
            AscensionCraft.LOGGER.debug("Received unknown tribulation {}", packet.tribulation());
            return;
        }
        TribulationData data = decodeData(tribulation, packet.data());
        if (data == null) return;
        remove(packet.id());
        TRIBULATIONS.put(packet.id(), new ClientTribulation(
                packet.id(),
                packet.tribulation(),
                tribulation,
                packet.dimension(),
                packet.position(),
                data
        ));
    }

    public static void update(ClientboundTribulationUpdatePacket packet) {
        ClientTribulation tribulation = TRIBULATIONS.get(packet.id());
        if (tribulation == null) return;
        TribulationData data = decodeData(tribulation.getTribulation(), packet.data());
        tribulation.update(packet.position(), data == null ? tribulation.getData() : data);
    }

    public static void remove(UUID id) {
        ClientTribulation tribulation = TRIBULATIONS.remove(id);
        if (tribulation == null) return;
        TribulationRenderer renderer = TribulationRenderers.get(tribulation.getTribulation().getType());
        if (renderer != null) renderer.onRemoved(tribulation);
    }

    public static void clear() {
        for (UUID id : java.util.List.copyOf(TRIBULATIONS.keySet())) {
            remove(id);
        }
    }

    private static @Nullable TribulationData decodeData(Tribulation tribulation, byte[] bytes) {
        StreamCodec<RegistryFriendlyByteBuf, ? extends TribulationData> codec = tribulation.getType().dataStreamCodec();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (bytes.length == 0 || connection == null) return null;

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), connection.registryAccess());
        try {
            return codec.decode(buf);
        } catch (Exception exception) {
            AscensionCraft.LOGGER.debug("Failed to decode tribulation data for {}", tribulation.getType(), exception);
            return null;
        } finally {
            buf.release();
        }
    }

    //──Events────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;
        for (ClientTribulation tribulation : TRIBULATIONS.values()) {
            if (!tribulation.getDimension().equals(minecraft.level.dimension())) continue;
            tribulation.tick();
            TribulationRenderer renderer = TribulationRenderers.get(tribulation.getTribulation().getType());
            if (renderer != null) renderer.clientTick(tribulation);
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || TRIBULATIONS.isEmpty()) return;
        for (ClientTribulation tribulation : TRIBULATIONS.values()) {
            if (!tribulation.getDimension().equals(minecraft.level.dimension())) continue;
            TribulationRenderer renderer = TribulationRenderers.get(tribulation.getTribulation().getType());
            if (renderer != null) renderer.render(tribulation, event);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    //the server resends tribulations for any chunks we track in the new dimension
    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        clear();
    }
}
