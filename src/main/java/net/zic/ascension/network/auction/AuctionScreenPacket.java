package net.zic.ascension.network.auction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.common.gui.AscensionGui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AuctionScreenPacket(
        Mode mode,
        BlockPos accessPos,
        UUID houseId,
        String ownerName,
        long inboxCurrency,
        List<AuctionViewData.HouseView> houses,
        List<AuctionViewData.AuctionView> auctions,
        List<AuctionViewData.InboxEntryView> inboxItems,
        UUID focusAuctionId
) implements CustomPacketPayload {
    public static final Type<AuctionScreenPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "auction_screen")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AuctionScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AuctionScreenPacket decode(RegistryFriendlyByteBuf buf) {
            Mode mode = buf.readEnum(Mode.class);
            BlockPos accessPos = BlockPos.of(buf.readLong());
            UUID houseId = readOptionalUuid(buf);
            String ownerName = buf.readUtf();
            long inboxCurrency = buf.readVarLong();

            int houseCount = buf.readVarInt();
            ArrayList<AuctionViewData.HouseView> houses = new ArrayList<>(houseCount);
            for (int i = 0; i < houseCount; i++) {
                UUID id = buf.readUUID();
                String owner = buf.readUtf();
                int auctionCount = buf.readVarInt();
                ArrayList<AuctionViewData.AuctionView> nested = new ArrayList<>(auctionCount);
                for (int j = 0; j < auctionCount; j++) {
                    nested.add(readAuction(buf));
                }
                houses.add(new AuctionViewData.HouseView(id, owner, nested));
            }

            int auctionCount = buf.readVarInt();
            ArrayList<AuctionViewData.AuctionView> auctions = new ArrayList<>(auctionCount);
            for (int i = 0; i < auctionCount; i++) {
                auctions.add(readAuction(buf));
            }

            int inboxCount = buf.readVarInt();
            ArrayList<AuctionViewData.InboxEntryView> inbox = new ArrayList<>(inboxCount);
            for (int i = 0; i < inboxCount; i++) {
                inbox.add(new AuctionViewData.InboxEntryView(buf.readUUID(), ItemStack.STREAM_CODEC.decode(buf)));
            }

            UUID focus = readOptionalUuid(buf);
            return new AuctionScreenPacket(mode, accessPos, houseId, ownerName, inboxCurrency, houses, auctions, inbox, focus);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AuctionScreenPacket packet) {
            buf.writeEnum(packet.mode());
            buf.writeLong(packet.accessPos().asLong());
            writeOptionalUuid(buf, packet.houseId());
            buf.writeUtf(packet.ownerName());
            buf.writeVarLong(packet.inboxCurrency());

            buf.writeVarInt(packet.houses().size());
            for (AuctionViewData.HouseView house : packet.houses()) {
                buf.writeUUID(house.id());
                buf.writeUtf(house.ownerName());
                buf.writeVarInt(house.auctions().size());
                for (AuctionViewData.AuctionView auction : house.auctions()) {
                    writeAuction(buf, auction);
                }
            }

            buf.writeVarInt(packet.auctions().size());
            for (AuctionViewData.AuctionView auction : packet.auctions()) {
                writeAuction(buf, auction);
            }

            buf.writeVarInt(packet.inboxItems().size());
            for (AuctionViewData.InboxEntryView entry : packet.inboxItems()) {
                buf.writeUUID(entry.id());
                ItemStack.STREAM_CODEC.encode(buf, entry.item());
            }
            writeOptionalUuid(buf, packet.focusAuctionId());
        }
    };

    public AuctionScreenPacket {
        accessPos = accessPos == null ? BlockPos.ZERO : accessPos.immutable();
        ownerName = ownerName == null ? "" : ownerName;
        houses = List.copyOf(houses);
        auctions = List.copyOf(auctions);
        inboxItems = List.copyOf(inboxItems);
    }

    private static AuctionViewData.AuctionView readAuction(RegistryFriendlyByteBuf buf) {
        return new AuctionViewData.AuctionView(
                buf.readUUID(),
                buf.readUUID(),
                buf.readUtf(),
                ItemStack.STREAM_CODEC.decode(buf),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readUtf(),
                buf.readVarLong()
        );
    }

    private static void writeAuction(RegistryFriendlyByteBuf buf, AuctionViewData.AuctionView auction) {
        buf.writeUUID(auction.id());
        buf.writeUUID(auction.houseId());
        buf.writeUtf(auction.sellerName());
        ItemStack.STREAM_CODEC.encode(buf, auction.item());
        buf.writeVarLong(auction.startingBid());
        buf.writeVarLong(auction.currentBid());
        buf.writeUtf(auction.highestBidderName());
        buf.writeVarLong(auction.endsAtMillis());
    }

    private static UUID readOptionalUuid(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }

    private static void writeOptionalUuid(RegistryFriendlyByteBuf buf, UUID id) {
        buf.writeBoolean(id != null);
        if (id != null) {
            buf.writeUUID(id);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuctionScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AscensionGui.openAuction(packet));
    }

    public enum Mode {
        OWNER,
        BIDDER,
        INBOX
    }
}
