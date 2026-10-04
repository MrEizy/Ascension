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

public record AuctionInboxActionPacket(Action action, UUID entryId, UUID houseId, BlockPos corePos) implements CustomPacketPayload {
    public static final Type<AuctionInboxActionPacket> TYPE = new Type<>(AscensionCraft.prefix("auction_inbox_action"));
    public static final StreamCodec<FriendlyByteBuf, AuctionInboxActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AuctionInboxActionPacket decode(FriendlyByteBuf buf) {
            Action action = buf.readEnum(Action.class);
            UUID entryId = readOptionalUuid(buf);
            UUID houseId = readOptionalUuid(buf);
            BlockPos corePos = BlockPos.of(buf.readLong());
            return new AuctionInboxActionPacket(action, entryId, houseId, corePos);
        }

        @Override
        public void encode(FriendlyByteBuf buf, AuctionInboxActionPacket packet) {
            buf.writeEnum(packet.action());
            writeOptionalUuid(buf, packet.entryId());
            writeOptionalUuid(buf, packet.houseId());
            buf.writeLong(packet.corePos().asLong());
        }
    };

    public AuctionInboxActionPacket {
        corePos = corePos == null ? BlockPos.ZERO : corePos.immutable();
    }

    public static AuctionInboxActionPacket open(UUID houseId, BlockPos corePos) {
        return new AuctionInboxActionPacket(Action.OPEN, null, houseId, corePos);
    }

    public static AuctionInboxActionPacket currency(UUID houseId, BlockPos corePos) {
        return new AuctionInboxActionPacket(Action.CURRENCY, null, houseId, corePos);
    }

    public static AuctionInboxActionPacket item(UUID entryId, UUID houseId, BlockPos corePos) {
        return new AuctionInboxActionPacket(Action.ITEM, entryId, houseId, corePos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuctionInboxActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AuctionManager manager = AuctionManager.getInstance();
            if (manager == null) {
                return;
            }

            if (packet.action() == Action.CURRENCY) {
                long claimed = manager.claimCurrency(player);
                if (claimed > 0L) {
                    player.sendSystemMessage(Component.translatable("auction.ascension.claimed_stones", claimed));
                }
            } else if (packet.action() == Action.ITEM && packet.entryId() != null) {
                manager.claimItem(player, packet.entryId());
            }

            if (packet.houseId() != null && manager.canManageHouse(player, packet.corePos(), packet.houseId())) {
                AuctionScreenSync.openOwnerInbox(player, packet.houseId(), packet.corePos());
            } else {
                AuctionScreenSync.openInbox(player);
            }
        });
    }

    private static UUID readOptionalUuid(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }

    private static void writeOptionalUuid(FriendlyByteBuf buf, UUID id) {
        buf.writeBoolean(id != null);
        if (id != null) {
            buf.writeUUID(id);
        }
    }

    public enum Action {
        OPEN,
        ITEM,
        CURRENCY
    }
}
