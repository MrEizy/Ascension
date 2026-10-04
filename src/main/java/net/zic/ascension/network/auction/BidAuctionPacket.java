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

public record BidAuctionPacket(BlockPos bidderPos, UUID auctionId, long amount) implements CustomPacketPayload {
    public static final Type<BidAuctionPacket> TYPE = new Type<>(AscensionCraft.prefix("bid_auction"));
    public static final StreamCodec<FriendlyByteBuf, BidAuctionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BidAuctionPacket decode(FriendlyByteBuf buf) {
            return new BidAuctionPacket(BlockPos.of(buf.readLong()), buf.readUUID(), buf.readVarLong());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BidAuctionPacket packet) {
            buf.writeLong(packet.bidderPos().asLong());
            buf.writeUUID(packet.auctionId());
            buf.writeVarLong(packet.amount());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BidAuctionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }
            AuctionManager.BidResult result = manager.placeBid(player, packet.bidderPos(), packet.auctionId(), packet.amount());
            player.sendSystemMessage(Component.translatable(switch (result) {
                case SUCCESS -> "auction.ascension.bid_success";
                case INVALID_ACCESS -> "auction.ascension.invalid_access";
                case ENDED -> "auction.ascension.auction_ended";
                case SELLER_CANNOT_BID -> "auction.ascension.seller_cannot_bid";
                case BID_TOO_LOW -> "auction.ascension.bid_too_low";
                case NOT_ENOUGH_CURRENCY -> "auction.ascension.not_enough_stones";
                case CURRENCY_UNAVAILABLE -> "auction.ascension.currency_unavailable";
            }));
            if (manager.canUseBidder(player, packet.bidderPos())) {
                AuctionScreenSync.openBidderBrowser(player, packet.bidderPos(), packet.auctionId());
            }
        });
    }
}
