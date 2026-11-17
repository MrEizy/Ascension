package net.zic.ascension.network.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;

public record ServerboundStabilizationHitPacket(BlockPos furnacePos, int targetIndex, int hitTick) implements CustomPacketPayload {
    public static final Type<ServerboundStabilizationHitPacket> TYPE =
            new Type<>(AscensionCraft.prefix("stabilization_hit"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundStabilizationHitPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.furnacePos());
                buf.writeVarInt(packet.targetIndex());
                buf.writeVarInt(packet.hitTick());
            },
            buf -> new ServerboundStabilizationHitPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundStabilizationHitPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.level().isLoaded(packet.furnacePos())
                    && player.level().getBlockEntity(packet.furnacePos()) instanceof AlchemyFurnaceBlockEntity furnace) {
                boolean accepted = furnace.hitStabilization(player, packet.targetIndex(), packet.hitTick());
                PacketDistributor.sendToPlayer(player, new ClientboundStabilizationHitResultPacket(
                        packet.furnacePos(), furnace.stabilizationSeed(), packet.targetIndex(), accepted));
            }
        });
    }
}
