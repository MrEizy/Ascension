package net.zic.ascension.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zic.ascension.AscensionCraft;


@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class DivineSenseNetworking {

    private DivineSenseNetworking() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ClientboundDivineSensePacket.TYPE,
                ClientboundDivineSensePacket.STREAM_CODEC,
                FMLEnvironment.getDist().isClient()
                        ? ClientboundDivineSensePacketHandler::handle
                        : DivineSenseNetworking::unreachableOnDedicatedServer
        );
    }

    private static void unreachableOnDedicatedServer(ClientboundDivineSensePacket packet, IPayloadContext context) {
        throw new IllegalStateException(
                "Divine Sense's client packet handler was invoked on a dedicated server — a playToClient packet should never reach here."
        );
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundDivineSensePacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}