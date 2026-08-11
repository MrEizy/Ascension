package net.zic.ascension.common.auction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AuctionManager extends SavedData {
    public static final int BIDDER_RADIUS = 20;
    public static final long MIN_DURATION_MILLIS = 30L * 60L * 1000L;
    public static final long MAX_DURATION_MILLIS = 7L * 24L * 60L * 60L * 1000L;

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final SavedDataType<AuctionManager> ID = new SavedDataType<>(
            AscensionCraft.prefix("auction_manager"),
            AuctionManager::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(UUID_CODEC, AuctionHouseData.CODEC)
                            .optionalFieldOf("houses", Map.of())
                            .forGetter(AuctionManager::housesForCodec),
                    Codec.unboundedMap(UUID_CODEC, AuctionListing.CODEC)
                            .optionalFieldOf("auctions", Map.of())
                            .forGetter(AuctionManager::auctionsForCodec),
                    Codec.unboundedMap(UUID_CODEC, AuctionInboxData.CODEC)
                            .optionalFieldOf("inboxes", Map.of())
                            .forGetter(AuctionManager::inboxesForCodec)
            ).apply(instance, AuctionManager::new))
    );

    private static AuctionManager instance;

    private final HashMap<UUID, AuctionHouseData> houses = new HashMap<>();
    private final HashMap<UUID, AuctionListing> auctions = new HashMap<>();
    private final HashMap<UUID, AuctionInboxData> inboxes = new HashMap<>();

    public AuctionManager() {
    }

    private AuctionManager(
            Map<UUID, AuctionHouseData> houses,
            Map<UUID, AuctionListing> auctions,
            Map<UUID, AuctionInboxData> inboxes
    ) {
        this.houses.putAll(houses);
        this.auctions.putAll(auctions);
        this.inboxes.putAll(inboxes);
        pruneMissingAuctionReferences();
    }

    public static AuctionManager getInstance() {
        return instance;
    }

    private Map<UUID, AuctionHouseData> housesForCodec() {
        return Map.copyOf(houses);
    }

    private Map<UUID, AuctionListing> auctionsForCodec() {
        return Map.copyOf(auctions);
    }

    private Map<UUID, AuctionInboxData> inboxesForCodec() {
        return Map.copyOf(inboxes);
    }

    public AuctionHouseData getHouse(UUID houseId) {
        return houses.get(houseId);
    }

    public AuctionListing getAuction(UUID auctionId) {
        return auctions.get(auctionId);
    }

    public AuctionInboxData getInbox(UUID playerId) {
        return inboxes.computeIfAbsent(playerId, ignored -> new AuctionInboxData());
    }

    public AuctionInboxData getInboxIfPresent(UUID playerId) {
        return inboxes.get(playerId);
    }

    public AuctionHouseData findHouse(Level level, BlockPos pos) {
        Identifier dimension = level.dimension().identifier();
        for (AuctionHouseData house : houses.values()) {
            if (house.dimension().equals(dimension) && house.corePos().equals(pos)) {
                return house;
            }
        }
        return null;
    }

    public synchronized AuctionHouseData registerHouse(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        AuctionHouseData existing = findHouse(level, pos);
        if (existing != null) {
            if (existing.ownerId().equals(owner.getUUID())) {
                AuctionHouseData renamed = existing.withOwnerName(owner.getName().getString());
                houses.put(renamed.id(), renamed);
                setDirty();
                return renamed;
            }
            return existing;
        }

        AuctionHouseData house = new AuctionHouseData(
                UUID.randomUUID(),
                owner.getUUID(),
                owner.getName().getString(),
                level.dimension().identifier(),
                pos.immutable(),
                List.of()
        );
        houses.put(house.id(), house);
        setDirty();
        return house;
    }

    public synchronized void removeHouseIfEmpty(Level level, BlockPos pos) {
        AuctionHouseData house = findHouse(level, pos);
        if (house == null || hasActiveAuctions(house.id())) {
            return;
        }
        houses.remove(house.id());
        setDirty();
    }

    public boolean hasActiveAuctions(UUID houseId) {
        AuctionHouseData house = houses.get(houseId);
        if (house == null) {
            return false;
        }
        for (UUID auctionId : house.auctionIds()) {
            if (auctions.containsKey(auctionId)) {
                return true;
            }
        }
        return false;
    }

    public boolean canManageHouse(ServerPlayer player, BlockPos corePos, UUID houseId) {
        if (!isPlayerNear(player, corePos, 8.0D)) {
            return false;
        }
        if (!player.level().getBlockState(corePos).is(ModBlocks.AUCTION_HOUSE_CORE.get())) {
            return false;
        }
        AuctionHouseData house = houses.get(houseId);
        return house != null
                && house.ownerId().equals(player.getUUID())
                && house.dimension().equals(player.level().dimension().identifier())
                && house.corePos().equals(corePos);
    }

    public boolean canUseBidder(ServerPlayer player, BlockPos bidderPos) {
        return isPlayerNear(player, bidderPos, 8.0D)
                && player.level().getBlockState(bidderPos).is(ModBlocks.AUCTION_BIDDER.get());
    }

    public boolean canAccessAuctionFromBidder(ServerPlayer player, BlockPos bidderPos, AuctionListing auction) {
        if (!canUseBidder(player, bidderPos) || auction == null) {
            return false;
        }
        AuctionHouseData house = houses.get(auction.houseId());
        return house != null && isHouseInBidderRange(player.level(), bidderPos, house);
    }

    public List<AuctionViewData.HouseView> nearbyHouseViews(ServerPlayer player, BlockPos bidderPos) {
        if (!canUseBidder(player, bidderPos)) {
            return List.of();
        }

        ArrayList<AuctionViewData.HouseView> result = new ArrayList<>();
        for (AuctionHouseData house : houses.values()) {
            if (!isHouseInBidderRange(player.level(), bidderPos, house)) {
                continue;
            }
            List<AuctionViewData.AuctionView> active = auctionViewsForHouse(house.id());
            if (!active.isEmpty()) {
                result.add(new AuctionViewData.HouseView(house.id(), house.ownerName(), active));
            }
        }
        result.sort(Comparator.comparing(AuctionViewData.HouseView::ownerName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(result);
    }

    public List<AuctionViewData.AuctionView> auctionViewsForHouse(UUID houseId) {
        AuctionHouseData house = houses.get(houseId);
        if (house == null) {
            return List.of();
        }
        ArrayList<AuctionViewData.AuctionView> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (UUID auctionId : house.auctionIds()) {
            AuctionListing listing = auctions.get(auctionId);
            if (listing != null && !listing.isExpired(now)) {
                result.add(toView(listing));
            }
        }
        result.sort(Comparator.comparingLong(AuctionViewData.AuctionView::endsAtMillis));
        return List.copyOf(result);
    }

    public AuctionViewData.AuctionView toView(AuctionListing listing) {
        return new AuctionViewData.AuctionView(
                listing.id(),
                listing.houseId(),
                listing.sellerName(),
                listing.itemStack(),
                listing.startingBid(),
                listing.currentBid(),
                listing.highestBidderName(),
                listing.endsAtMillis()
        );
    }

    public List<AuctionViewData.InboxEntryView> inboxViews(UUID playerId) {
        AuctionInboxData inbox = inboxes.get(playerId);
        if (inbox == null) {
            return List.of();
        }
        return inbox.entries().stream()
                .map(entry -> new AuctionViewData.InboxEntryView(entry.id(), entry.item().create()))
                .toList();
    }

    public synchronized CreateResult createAuction(
            ServerPlayer seller,
            UUID houseId,
            BlockPos corePos,
            int inventorySlot,
            int quantity,
            long startingBid,
            long durationMillis
    ) {
        if (!canManageHouse(seller, corePos, houseId)) {
            return CreateResult.INVALID_ACCESS;
        }
        if (inventorySlot < 0 || inventorySlot >= Math.min(AuctionCurrency.INVENTORY_SLOT_COUNT, seller.getInventory().getContainerSize())) {
            return CreateResult.INVALID_ITEM;
        }
        if (quantity <= 0 || startingBid <= 0L) {
            return CreateResult.INVALID_VALUES;
        }
        if (durationMillis < MIN_DURATION_MILLIS || durationMillis > MAX_DURATION_MILLIS) {
            return CreateResult.INVALID_VALUES;
        }

        ItemStack source = seller.getInventory().getItem(inventorySlot);
        if (source.isEmpty() || source.getCount() < quantity) {
            return CreateResult.INVALID_ITEM;
        }

        ItemStack auctionStack = source.copyWithCount(quantity);
        source.shrink(quantity);
        seller.getInventory().setChanged();

        long now = System.currentTimeMillis();
        long endsAt = now + durationMillis;
        AuctionListing listing = AuctionListing.create(
                houseId,
                seller.getUUID(),
                seller.getName().getString(),
                auctionStack,
                startingBid,
                endsAt
        );
        auctions.put(listing.id(), listing);

        AuctionHouseData house = houses.get(houseId);
        houses.put(houseId, house.withAuction(listing.id()).withOwnerName(seller.getName().getString()));
        setDirty();
        return CreateResult.SUCCESS;
    }

    public synchronized BidResult placeBid(
            ServerPlayer bidder,
            BlockPos bidderPos,
            UUID auctionId,
            long amount
    ) {
        AuctionListing listing = auctions.get(auctionId);
        if (!canAccessAuctionFromBidder(bidder, bidderPos, listing)) {
            return BidResult.INVALID_ACCESS;
        }
        if (listing.isExpired(System.currentTimeMillis())) {
            return BidResult.ENDED;
        }
        if (listing.sellerId().equals(bidder.getUUID())) {
            return BidResult.SELLER_CANNOT_BID;
        }
        if (amount < listing.minimumNextBid()) {
            return BidResult.BID_TOO_LOW;
        }

        long alreadyEscrowed = listing.escrowedBy(bidder.getUUID());
        long additional = amount - alreadyEscrowed;
        if (additional < 0L) {
            return BidResult.BID_TOO_LOW;
        }
        if (!AuctionCurrency.remove(bidder, additional)) {
            return AuctionCurrency.isAvailable() ? BidResult.NOT_ENOUGH_CURRENCY : BidResult.CURRENCY_UNAVAILABLE;
        }

        UUID previousWinner = listing.highestBidder();
        listing.placeBid(bidder.getUUID(), bidder.getName().getString(), amount);
        setDirty();

        if (previousWinner != null && !previousWinner.equals(bidder.getUUID())) {
            MinecraftServer server = bidder.level().getServer();
            if (server != null) {
                ServerPlayer previous = server.getPlayerList().getPlayer(previousWinner);
                if (previous != null) {
                    previous.sendSystemMessage(Component.translatable(
                            "auction.ascension.outbid",
                            listing.itemStack().getHoverName()
                    ));
                }
            }
        }
        return BidResult.SUCCESS;
    }

    public synchronized void resolveExpired(MinecraftServer server) {
        long now = System.currentTimeMillis();
        ArrayList<UUID> expired = new ArrayList<>();
        for (AuctionListing listing : auctions.values()) {
            if (listing.isExpired(now)) {
                expired.add(listing.id());
            }
        }
        if (expired.isEmpty()) {
            return;
        }

        for (UUID auctionId : expired) {
            resolveAuction(server, auctionId);
        }
        setDirty();
    }

    private void resolveAuction(MinecraftServer server, UUID auctionId) {
        AuctionListing listing = auctions.remove(auctionId);
        if (listing == null) {
            return;
        }

        AuctionHouseData house = houses.get(listing.houseId());
        if (house != null) {
            houses.put(house.id(), house.withoutAuction(auctionId));
        }

        if (!listing.hasBid()) {
            getInbox(listing.sellerId()).addItem(listing.itemStack());
            notifyOnline(server, listing.sellerId(), Component.translatable(
                    "auction.ascension.ended_no_bid",
                    listing.itemStack().getHoverName()
            ));
            return;
        }

        UUID winner = listing.highestBidder();
        getInbox(winner).addItem(listing.itemStack());
        getInbox(listing.sellerId()).addCurrency(listing.currentBid());

        for (Map.Entry<UUID, Long> entry : listing.escrow().entrySet()) {
            if (!entry.getKey().equals(winner)) {
                getInbox(entry.getKey()).addCurrency(entry.getValue());
            }
        }

        notifyOnline(server, winner, Component.translatable(
                "auction.ascension.won",
                listing.itemStack().getHoverName()
        ));
        notifyOnline(server, listing.sellerId(), Component.translatable(
                "auction.ascension.sold",
                listing.itemStack().getHoverName(),
                listing.currentBid()
        ));
    }

    public synchronized long claimCurrency(ServerPlayer player) {
        AuctionInboxData inbox = inboxes.get(player.getUUID());
        if (inbox == null || inbox.currency() <= 0L) {
            return 0L;
        }
        long inserted = AuctionCurrency.give(player, inbox.currency());
        if (inserted > 0L) {
            inbox.removeCurrency(inserted);
            cleanupInbox(player.getUUID(), inbox);
            setDirty();
        }
        return inserted;
    }

    public synchronized boolean claimItem(ServerPlayer player, UUID entryId) {
        AuctionInboxData inbox = inboxes.get(player.getUUID());
        if (inbox == null) {
            return false;
        }
        AuctionInboxEntry entry = inbox.getEntry(entryId);
        if (entry == null) {
            return false;
        }

        ItemStack stack = entry.item().create();
        int before = stack.getCount();
        player.getInventory().add(stack);
        if (stack.getCount() == before) {
            return false;
        }

        inbox.replaceEntry(entryId, stack);
        cleanupInbox(player.getUUID(), inbox);
        player.getInventory().setChanged();
        setDirty();
        return true;
    }

    private void cleanupInbox(UUID playerId, AuctionInboxData inbox) {
        if (inbox.isEmpty()) {
            inboxes.remove(playerId);
        }
    }

    private boolean isHouseInBidderRange(Level level, BlockPos bidderPos, AuctionHouseData house) {
        if (!house.dimension().equals(level.dimension().identifier())) {
            return false;
        }
        long dx = (long) bidderPos.getX() - house.corePos().getX();
        long dy = (long) bidderPos.getY() - house.corePos().getY();
        long dz = (long) bidderPos.getZ() - house.corePos().getZ();
        if (dx * dx + dy * dy + dz * dz > (long) BIDDER_RADIUS * BIDDER_RADIUS) {
            return false;
        }
        return level.getBlockState(house.corePos()).is(ModBlocks.AUCTION_HOUSE_CORE.get());
    }

    private static boolean isPlayerNear(ServerPlayer player, BlockPos pos, double maxDistance) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        return player.distanceToSqr(x, y, z) <= maxDistance * maxDistance;
    }

    private static void notifyOnline(MinecraftServer server, UUID playerId, Component message) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            player.sendSystemMessage(message);
        }
    }

    private void pruneMissingAuctionReferences() {
        for (Map.Entry<UUID, AuctionHouseData> entry : new ArrayList<>(houses.entrySet())) {
            AuctionHouseData house = entry.getValue();
            List<UUID> valid = house.auctionIds().stream().filter(auctions::containsKey).toList();
            if (!valid.equals(house.auctionIds())) {
                houses.put(entry.getKey(), new AuctionHouseData(
                        house.id(), house.ownerId(), house.ownerName(), house.dimension(), house.corePos(), valid
                ));
                setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        event.getServer().getDataStorage().computeIfAbsent(ID);
        instance = event.getServer().getDataStorage().get(ID);
        if (instance != null) {
            instance.resolveExpired(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        instance = null;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        AuctionManager manager = instance;
        if (manager == null || event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        manager.resolveExpired(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AuctionManager manager = instance;
        if (manager == null) {
            return;
        }
        AuctionInboxData inbox = manager.getInboxIfPresent(player.getUUID());
        if (inbox != null && !inbox.isEmpty()) {
            player.sendSystemMessage(Component.translatable("auction.ascension.inbox_waiting"));
        }
    }

    @SubscribeEvent
    public static void onCoreLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        AuctionManager manager = instance;
        if (manager == null || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.AUCTION_HOUSE_CORE.get())) {
            return;
        }

        AuctionHouseData house = manager.findHouse(player.level(), event.getPos());
        if (house == null) {
            return;
        }

        if (manager.hasActiveAuctions(house.id())) {
            event.setCanceled(true);
            if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
                player.sendSystemMessage(Component.translatable("auction.ascension.core_has_active"));
            }
            return;
        }
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.STOP) {
            manager.removeHouseIfEmpty(player.level(), event.getPos());
        }
    }


    public enum CreateResult {
        SUCCESS,
        INVALID_ACCESS,
        INVALID_ITEM,
        INVALID_VALUES
    }

    public enum BidResult {
        SUCCESS,
        INVALID_ACCESS,
        ENDED,
        SELLER_CANNOT_BID,
        BID_TOO_LOW,
        NOT_ENOUGH_CURRENCY,
        CURRENCY_UNAVAILABLE
    }
}
