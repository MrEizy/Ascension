package net.zic.ascension.common.auction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.UUID;

public record AuctionInboxEntry(UUID id, ItemStackTemplate item) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<AuctionInboxEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUID_CODEC.fieldOf("id").forGetter(AuctionInboxEntry::id),
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(AuctionInboxEntry::item)
    ).apply(instance, AuctionInboxEntry::new));
}
