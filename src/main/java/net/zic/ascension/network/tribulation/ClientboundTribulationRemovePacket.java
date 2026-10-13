package net.zic.ascension.network.tribulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.tribulation.client.ClientTribulationManager;

import java.util.UUID;

public record ClientboundTribulationRemovePacket(UUID id) implements CustomPacketPayload {
    public static final Type<ClientboundTribulationRemovePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation_remove")
    );

    public static final StreamCodec<FriendlyByteBuf, ClientboundTribulationRemovePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUUID(packet.id()),
            buf -> new ClientboundTribulationRemovePacket(buf.readUUID())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundTribulationRemovePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTribulationManager.remove(packet.id()));
    }
}
