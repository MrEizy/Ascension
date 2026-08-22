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

/**
 * Registers ClientboundDivineSensePacket and exposes the send helper the
 * feature calls.
 *
 * RegisterPayloadHandlersEvent fires on BOTH sides. Passing
 * ClientboundDivineSensePacketHandler::handle directly here used to be safe
 * because @OnlyIn stripped that class before the dedicated server could ever
 * touch it. That stripping is gone in 26.1, so the method reference alone
 * would force-resolve Minecraft (a client-only class) on load, even on a
 * dedicated server, since a playToClient packet is still registered on both
 * sides. The real handler is only ever referenced when actually running on
 * the client; the server gets a stub it will never call, since it never
 * receives its own client-bound packets.
 *
 * If Ascension already has a central networking registration class, fold
 * this @SubscribeEvent method into it instead of keeping a second
 * RegisterPayloadHandlersEvent listener.
 */
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