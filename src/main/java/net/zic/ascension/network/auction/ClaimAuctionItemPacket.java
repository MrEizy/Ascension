package net.zic.ascension.network.auction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionScreenSync;

import java.util.UUID;

public record ClaimAuctionItemPacket(UUID entryId) implements CustomPacketPayload {
    public static final Type<ClaimAuctionItemPacket> TYPE = new Type<>(AscensionCraft.prefix("claim_auction_item"));
    public static final StreamCodec<FriendlyByteBuf, ClaimAuctionItemPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaimAuctionItemPacket decode(FriendlyByteBuf buf) {
            return new ClaimAuctionItemPacket(buf.readUUID());
        }

        @Override
        public void encode(FriendlyByteBuf buf, ClaimAuctionItemPacket packet) {
            buf.writeUUID(packet.entryId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClaimAuctionItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }
            manager.claimItem(player, packet.entryId());
            AuctionScreenSync.openInbox(player);
        });
    }
}
