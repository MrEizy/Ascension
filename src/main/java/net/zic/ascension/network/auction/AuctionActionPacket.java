package net.zic.ascension.network.auction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionHouseData;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionScreenSync;

import java.util.UUID;

public record AuctionActionPacket(Action action, BlockPos accessPos, UUID auctionId) implements CustomPacketPayload {
    public static final Type<AuctionActionPacket> TYPE = new Type<>(AscensionCraft.prefix("auction_action"));

    public static final StreamCodec<FriendlyByteBuf, AuctionActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AuctionActionPacket decode(FriendlyByteBuf buf) {
            return new AuctionActionPacket(buf.readEnum(Action.class), BlockPos.of(buf.readLong()), buf.readUUID());
        }

        @Override
        public void encode(FriendlyByteBuf buf, AuctionActionPacket packet) {
            buf.writeEnum(packet.action());
            buf.writeLong(packet.accessPos().asLong());
            buf.writeUUID(packet.auctionId());
        }
    };

    public AuctionActionPacket {
        accessPos = accessPos == null ? BlockPos.ZERO : accessPos.immutable();
    }

    public static AuctionActionPacket cancel(BlockPos corePos, UUID auctionId) {
        return new AuctionActionPacket(Action.CANCEL, corePos, auctionId);
    }

    public static AuctionActionPacket toggleNotifications(BlockPos bidderPos, UUID auctionId) {
        return new AuctionActionPacket(Action.TOGGLE_NOTIFICATIONS, bidderPos, auctionId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuctionActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }

            if (packet.action() == Action.CANCEL) {
                AuctionManager.CancelResult result = manager.cancelAuction(player, packet.accessPos(), packet.auctionId());
                player.sendSystemMessage(Component.translatable(switch (result) {
                    case SUCCESS -> "auction.ascension.cancel_success";
                    case INVALID_ACCESS -> "auction.ascension.invalid_access";
                    case HAS_BIDS -> "auction.ascension.cancel_has_bids";
                    case NOT_FOUND -> "auction.ascension.auction_ended";
                }));

                AuctionHouseData house = manager.findHouse(player.level(), packet.accessPos());
                if (house != null && manager.canManageHouse(player, packet.accessPos(), house.id())) {
                    AuctionScreenSync.openOwnerAuctions(player, house.id(), packet.accessPos());
                }
                return;
            }

            AuctionManager.NotificationResult result = manager.toggleNotifications(
                    player,
                    packet.accessPos(),
                    packet.auctionId()
            );
            player.sendSystemMessage(Component.translatable(switch (result) {
                case ENABLED -> "auction.ascension.notifications_enabled";
                case DISABLED -> "auction.ascension.notifications_disabled";
                case INVALID_ACCESS -> "auction.ascension.invalid_access";
                case ENDED -> "auction.ascension.auction_ended";
            }));

            if (manager.canUseBidder(player, packet.accessPos())) {
                AuctionScreenSync.openBidderBrowser(player, packet.accessPos(), packet.auctionId());
            }
        });
    }

    public enum Action {
        CANCEL,
        TOGGLE_NOTIFICATIONS
    }
}
