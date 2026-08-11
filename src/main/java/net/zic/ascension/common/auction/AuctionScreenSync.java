package net.zic.ascension.common.auction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.network.auction.AuctionScreenPacket;

import java.util.List;
import java.util.UUID;

public final class AuctionScreenSync {
    private AuctionScreenSync() {
    }

    public static void openOwnerHome(ServerPlayer player, AuctionHouseData house) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null || house == null || !manager.canManageHouse(player, house.corePos(), house.id())) {
            return;
        }
        AuctionInboxData inbox = manager.getInboxIfPresent(player.getUUID());
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.OWNER,
                house.corePos(),
                house.id(),
                house.ownerName(),
                inbox == null ? 0L : inbox.currency(),
                List.of(),
                manager.auctionViewsForHouse(house.id()),
                List.of(),
                null
        ));
    }

    public static void openOwnerHome(ServerPlayer player, UUID houseId, BlockPos corePos) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null || !manager.canManageHouse(player, corePos, houseId)) {
            return;
        }
        openOwnerHome(player, manager.getHouse(houseId));
    }

    public static void openOwnerAuctions(ServerPlayer player, UUID houseId, BlockPos corePos) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null || !manager.canManageHouse(player, corePos, houseId)) {
            return;
        }
        AuctionHouseData house = manager.getHouse(houseId);
        if (house == null) {
            return;
        }
        AuctionInboxData inbox = manager.getInboxIfPresent(player.getUUID());
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.OWNER_AUCTIONS,
                house.corePos(),
                house.id(),
                house.ownerName(),
                inbox == null ? 0L : inbox.currency(),
                List.of(),
                manager.auctionViewsForHouse(house.id(), player.getUUID()),
                List.of(),
                null
        ));
    }

    public static void openBidderBrowser(ServerPlayer player, BlockPos bidderPos, UUID focusAuctionId) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null || !manager.canUseBidder(player, bidderPos)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.BIDDER,
                bidderPos,
                null,
                "",
                0L,
                manager.nearbyHouseViews(player, bidderPos),
                List.of(),
                List.of(),
                focusAuctionId
        ));
    }

    public static void openInbox(ServerPlayer player) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null) {
            return;
        }
        AuctionInboxData inbox = manager.getInboxIfPresent(player.getUUID());
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.INBOX,
                BlockPos.ZERO,
                null,
                player.getName().getString(),
                inbox == null ? 0L : inbox.currency(),
                List.of(),
                List.of(),
                manager.inboxViews(player.getUUID()),
                null
        ));
    }

    public static void openOwnerInbox(ServerPlayer player, UUID houseId, BlockPos corePos) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null || !manager.canManageHouse(player, corePos, houseId)) {
            openInbox(player);
            return;
        }
        AuctionHouseData house = manager.getHouse(houseId);
        if (house == null) {
            openInbox(player);
            return;
        }
        AuctionInboxData inbox = manager.getInboxIfPresent(player.getUUID());
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.OWNER_INBOX,
                house.corePos(),
                house.id(),
                house.ownerName(),
                inbox == null ? 0L : inbox.currency(),
                List.of(),
                manager.auctionViewsForHouse(house.id()),
                manager.inboxViews(player.getUUID()),
                null
        ));
    }

    public static void openBids(ServerPlayer player) {
        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new AuctionScreenPacket(
                AuctionScreenPacket.Mode.BIDS,
                BlockPos.ZERO,
                null,
                player.getName().getString(),
                0L,
                List.of(),
                manager.activeBidViews(player.getUUID()),
                List.of(),
                null
        ));
    }
}
