package net.zic.ascension.network.auction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionScreenSync;

public record ClaimAuctionCurrencyPacket() implements CustomPacketPayload {
    public static final Type<ClaimAuctionCurrencyPacket> TYPE = new Type<>(AscensionCraft.prefix("claim_auction_currency"));
    public static final StreamCodec<FriendlyByteBuf, ClaimAuctionCurrencyPacket> STREAM_CODEC = StreamCodec.unit(new ClaimAuctionCurrencyPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClaimAuctionCurrencyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }
            long claimed = manager.claimCurrency(player);
            if (claimed > 0L) {
                player.sendSystemMessage(Component.translatable("auction.ascension.claimed_stones", claimed));
            }
            AuctionScreenSync.openInbox(player);
        });
    }
}
