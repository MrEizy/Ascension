package net.zic.ascension.network.auction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionScreenSync;

import java.util.UUID;

public record CreateAuctionPacket(
        UUID houseId,
        BlockPos corePos,
        int inventorySlot,
        int quantity,
        long startingBid,
        long durationMillis
) implements CustomPacketPayload {
    public static final Type<CreateAuctionPacket> TYPE = new Type<>(AscensionCraft.prefix("create_auction"));
    public static final StreamCodec<FriendlyByteBuf, CreateAuctionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CreateAuctionPacket decode(FriendlyByteBuf buf) {
            return new CreateAuctionPacket(
                    buf.readUUID(),
                    BlockPos.of(buf.readLong()),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarLong(),
                    buf.readVarLong()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, CreateAuctionPacket packet) {
            buf.writeUUID(packet.houseId());
            buf.writeLong(packet.corePos().asLong());
            buf.writeVarInt(packet.inventorySlot());
            buf.writeVarInt(packet.quantity());
            buf.writeVarLong(packet.startingBid());
            buf.writeVarLong(packet.durationMillis());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreateAuctionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }
            AuctionManager.CreateResult result = manager.createAuction(
                    player,
                    packet.houseId(),
                    packet.corePos(),
                    packet.inventorySlot(),
                    packet.quantity(),
                    packet.startingBid(),
                    packet.durationMillis()
            );
            player.sendSystemMessage(Component.translatable(switch (result) {
                case SUCCESS -> "auction.ascension.create_success";
                case INVALID_ACCESS -> "auction.ascension.invalid_access";
                case INVALID_ITEM -> "auction.ascension.invalid_item";
                case INVALID_VALUES -> "auction.ascension.invalid_values";
            }));
            if (manager.canManageHouse(player, packet.corePos(), packet.houseId())) {
                AuctionScreenSync.openOwnerHome(player, packet.houseId(), packet.corePos());
            }
        });
    }
}
