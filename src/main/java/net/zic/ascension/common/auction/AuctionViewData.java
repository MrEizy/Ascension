package net.zic.ascension.common.auction;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class AuctionViewData {
    private AuctionViewData() {
    }

    public record AuctionView(
            UUID id,
            UUID houseId,
            String sellerName,
            ItemStack item,
            long startingBid,
            long currentBid,
            String highestBidderName,
            long endsAtMillis,
            long viewerEscrow,
            boolean viewerNotifications
    ) {
        public AuctionView {
            item = item.copy();
            sellerName = sellerName == null ? "" : sellerName;
            highestBidderName = highestBidderName == null ? "" : highestBidderName;
            viewerEscrow = Math.max(0L, viewerEscrow);
        }

        public long minimumNextBid() {
            return currentBid > 0L ? currentBid + 1L : startingBid;
        }

        public boolean viewerIsWinning() {
            return viewerEscrow > 0L && currentBid == viewerEscrow;
        }
    }

    public record HouseView(
            UUID id,
            String ownerName,
            List<AuctionView> auctions
    ) {
        public HouseView {
            ownerName = ownerName == null ? "" : ownerName;
            auctions = List.copyOf(auctions);
        }
    }

    public record InboxEntryView(UUID id, ItemStack item) {
        public InboxEntryView {
            item = item.copy();
        }
    }
}
