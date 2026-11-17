package net.zic.ascension.network.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.alchemy.AlchemyStabilizationClient;

public record ClientboundStabilizationHitResultPacket(BlockPos furnacePos, long seed, int targetIndex,
                                                        boolean accepted) implements CustomPacketPayload {
    public static final Type<ClientboundStabilizationHitResultPacket> TYPE =
            new Type<>(AscensionCraft.prefix("stabilization_hit_result"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundStabilizationHitResultPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.furnacePos());
                buf.writeLong(packet.seed());
                buf.writeVarInt(packet.targetIndex());
                buf.writeBoolean(packet.accepted());
            },
            buf -> new ClientboundStabilizationHitResultPacket(buf.readBlockPos(), buf.readLong(),
                    buf.readVarInt(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundStabilizationHitResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AlchemyStabilizationClient.onHitResult(
                packet.furnacePos(), packet.seed(), packet.targetIndex(), packet.accepted()));
    }
}
