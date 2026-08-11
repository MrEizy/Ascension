package net.zic.ascension.common.auction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AuctionInboxData {
    public static final Codec<AuctionInboxData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("currency", 0L).forGetter(AuctionInboxData::currency),
            AuctionInboxEntry.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(AuctionInboxData::entries)
    ).apply(instance, AuctionInboxData::new));

    private long currency;
    private final ArrayList<AuctionInboxEntry> entries = new ArrayList<>();

    public AuctionInboxData() {
    }

    public AuctionInboxData(long currency, List<AuctionInboxEntry> entries) {
        this.currency = Math.max(0L, currency);
        this.entries.addAll(entries);
    }

    public long currency() {
        return currency;
    }

    public List<AuctionInboxEntry> entries() {
        return List.copyOf(entries);
    }

    public boolean isEmpty() {
        return currency <= 0L && entries.isEmpty();
    }

    public void addCurrency(long amount) {
        if (amount > 0L) {
            currency = amount > Long.MAX_VALUE - currency ? Long.MAX_VALUE : currency + amount;
        }
    }

    public long removeCurrency(long amount) {
        long removed = Math.min(currency, Math.max(0L, amount));
        currency -= removed;
        return removed;
    }

    public void addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        entries.add(new AuctionInboxEntry(
                UUID.randomUUID(),
                ItemStackTemplate.fromNonEmptyStack(stack.copy())
        ));
    }

    public AuctionInboxEntry getEntry(UUID entryId) {
        for (AuctionInboxEntry entry : entries) {
            if (entry.id().equals(entryId)) {
                return entry;
            }
        }
        return null;
    }

    public void removeEntry(UUID entryId) {
        entries.removeIf(entry -> entry.id().equals(entryId));
    }

    public void replaceEntry(UUID entryId, ItemStack remainder) {
        for (int i = 0; i < entries.size(); i++) {
            AuctionInboxEntry entry = entries.get(i);
            if (!entry.id().equals(entryId)) {
                continue;
            }
            if (remainder == null || remainder.isEmpty()) {
                entries.remove(i);
            } else {
                entries.set(i, new AuctionInboxEntry(
                        entryId,
                        ItemStackTemplate.fromNonEmptyStack(remainder.copy())
                ));
            }
            return;
        }
    }
}
