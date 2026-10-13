package net.zic.ascension.network.tribulation;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.tribulation.ClientTribulationManager;

import java.util.UUID;

public record ClientboundTribulationUpdatePacket(
        UUID id,
        Vec3 position,
        ByteBuf data
) implements CustomPacketPayload {
    public static final Type<ClientboundTribulationUpdatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation_update")
    );

    public static final StreamCodec<FriendlyByteBuf, ClientboundTribulationUpdatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.id());
                TribulationPacketHelper.writeVec3(buf, packet.position());
                TribulationPacketHelper.writeData(buf, packet.data());
            },
            buf -> new ClientboundTribulationUpdatePacket(
                    buf.readUUID(),
                    TribulationPacketHelper.readVec3(buf),
                    TribulationPacketHelper.readData(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundTribulationUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTribulationManager.update(packet));
    }
}
