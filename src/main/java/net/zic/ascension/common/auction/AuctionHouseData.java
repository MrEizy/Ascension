package net.zic.ascension.common.auction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AuctionHouseData(
        UUID id,
        UUID ownerId,
        String ownerName,
        Identifier dimension,
        BlockPos corePos,
        List<UUID> auctionIds
) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<AuctionHouseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(AuctionHouseData::id),
            UUID_CODEC.fieldOf("owner").forGetter(AuctionHouseData::ownerId),
            Codec.STRING.fieldOf("owner_name").forGetter(AuctionHouseData::ownerName),
            Identifier.CODEC.fieldOf("dimension").forGetter(AuctionHouseData::dimension),
            BlockPos.CODEC.fieldOf("core_pos").forGetter(AuctionHouseData::corePos),
            UUID_CODEC.listOf().optionalFieldOf("auctions", List.of()).forGetter(AuctionHouseData::auctionIds)
    ).apply(instance, AuctionHouseData::new));

    public AuctionHouseData {
        auctionIds = List.copyOf(auctionIds);
    }

    public AuctionHouseData withOwnerName(String name) {
        return new AuctionHouseData(id, ownerId, name, dimension, corePos, auctionIds);
    }

    public AuctionHouseData withAuction(UUID auctionId) {
        ArrayList<UUID> updated = new ArrayList<>(auctionIds);
        if (!updated.contains(auctionId)) {
            updated.add(auctionId);
        }
        return new AuctionHouseData(id, ownerId, ownerName, dimension, corePos, updated);
    }

    public AuctionHouseData withoutAuction(UUID auctionId) {
        ArrayList<UUID> updated = new ArrayList<>(auctionIds);
        updated.remove(auctionId);
        return new AuctionHouseData(id, ownerId, ownerName, dimension, corePos, updated);
    }
}
