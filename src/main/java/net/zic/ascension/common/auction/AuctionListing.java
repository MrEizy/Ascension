package net.zic.ascension.common.auction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AuctionListing {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<AuctionListing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(AuctionListing::id),
            UUID_CODEC.fieldOf("house").forGetter(AuctionListing::houseId),
            UUID_CODEC.fieldOf("seller").forGetter(AuctionListing::sellerId),
            Codec.STRING.fieldOf("seller_name").forGetter(AuctionListing::sellerName),
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(AuctionListing::itemTemplate),
            Codec.LONG.fieldOf("starting_bid").forGetter(AuctionListing::startingBid),
            Codec.LONG.optionalFieldOf("current_bid", 0L).forGetter(AuctionListing::currentBid),
            UUID_CODEC.optionalFieldOf("highest_bidder").forGetter(value -> Optional.ofNullable(value.highestBidder)),
            Codec.STRING.optionalFieldOf("highest_bidder_name", "").forGetter(AuctionListing::highestBidderName),
            Codec.LONG.fieldOf("ends_at").forGetter(AuctionListing::endsAtMillis),
            Codec.unboundedMap(UUID_CODEC, Codec.LONG).optionalFieldOf("escrow", Map.of()).forGetter(AuctionListing::escrow)
    ).apply(instance, (id, houseId, sellerId, sellerName, item, startingBid, currentBid, highestBidder, highestBidderName, endsAt, escrow) ->
            new AuctionListing(id, houseId, sellerId, sellerName, item, startingBid, currentBid,
                    highestBidder.orElse(null), highestBidderName, endsAt, escrow)
    ));

    private final UUID id;
    private final UUID houseId;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStackTemplate itemTemplate;
    private final long startingBid;
    private long currentBid;
    private UUID highestBidder;
    private String highestBidderName;
    private final long endsAtMillis;
    private final HashMap<UUID, Long> escrow = new HashMap<>();

    public AuctionListing(
            UUID id,
            UUID houseId,
            UUID sellerId,
            String sellerName,
            ItemStackTemplate itemTemplate,
            long startingBid,
            long currentBid,
            UUID highestBidder,
            String highestBidderName,
            long endsAtMillis,
            Map<UUID, Long> escrow
    ) {
        this.id = id;
        this.houseId = houseId;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.itemTemplate = itemTemplate;
        this.startingBid = Math.max(1L, startingBid);
        this.currentBid = Math.max(0L, currentBid);
        this.highestBidder = highestBidder;
        this.highestBidderName = highestBidderName == null ? "" : highestBidderName;
        this.endsAtMillis = endsAtMillis;
        this.escrow.putAll(escrow);
    }

    public static AuctionListing create(
            UUID houseId,
            UUID sellerId,
            String sellerName,
            ItemStack stack,
            long startingBid,
            long endsAtMillis
    ) {
        return new AuctionListing(
                UUID.randomUUID(),
                houseId,
                sellerId,
                sellerName,
                ItemStackTemplate.fromNonEmptyStack(stack.copy()),
                startingBid,
                0L,
                null,
                "",
                endsAtMillis,
                Map.of()
        );
    }

    public UUID id() { return id; }
    public UUID houseId() { return houseId; }
    public UUID sellerId() { return sellerId; }
    public String sellerName() { return sellerName; }
    public ItemStackTemplate itemTemplate() { return itemTemplate; }
    public ItemStack itemStack() { return itemTemplate.create(); }
    public long startingBid() { return startingBid; }
    public long currentBid() { return currentBid; }
    public UUID highestBidder() { return highestBidder; }
    public String highestBidderName() { return highestBidderName; }
    public long endsAtMillis() { return endsAtMillis; }
    public Map<UUID, Long> escrow() { return Map.copyOf(escrow); }

    public boolean hasBid() {
        return highestBidder != null && currentBid > 0L;
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis >= endsAtMillis;
    }

    public long minimumNextBid() {
        return hasBid() ? currentBid + 1L : startingBid;
    }

    public long escrowedBy(UUID bidder) {
        return escrow.getOrDefault(bidder, 0L);
    }

    public void placeBid(UUID bidder, String bidderName, long amount) {
        escrow.put(bidder, amount);
        highestBidder = bidder;
        highestBidderName = bidderName == null ? "" : bidderName;
        currentBid = amount;
    }
}
