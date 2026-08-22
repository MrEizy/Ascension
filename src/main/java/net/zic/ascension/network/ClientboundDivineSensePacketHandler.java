package net.zic.ascension.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.client.visual.DivineSenseClientState;


public final class ClientboundDivineSensePacketHandler {
    private ClientboundDivineSensePacketHandler() {
    }

    public static void handle(ClientboundDivineSensePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            DivineSenseClientState.get().start(
                    packet.center(),
                    packet.radius(),
                    packet.durationTicks(),
                    packet.color(),
                    packet.entityIds()
            );
        });
    }
}