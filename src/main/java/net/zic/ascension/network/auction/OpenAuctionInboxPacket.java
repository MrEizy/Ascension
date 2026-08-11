package net.zic.ascension.network.auction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionScreenSync;

public record OpenAuctionInboxPacket() implements CustomPacketPayload {
    public static final Type<OpenAuctionInboxPacket> TYPE = new Type<>(AscensionCraft.prefix("open_auction_inbox"));
    public static final StreamCodec<FriendlyByteBuf, OpenAuctionInboxPacket> STREAM_CODEC = StreamCodec.unit(new OpenAuctionInboxPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAuctionInboxPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AuctionScreenSync.openInbox(player);
            }
        });
    }
}
