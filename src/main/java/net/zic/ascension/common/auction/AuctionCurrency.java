package net.zic.ascension.common.auction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.ascension.AscensionCraft;

public final class AuctionCurrency {
    public static final Identifier SPIRITUAL_STONE_ID = AscensionCraft.prefix("spiritual_stone");
    public static final int INVENTORY_SLOT_COUNT = 36;

    private AuctionCurrency() {
    }

    public static Item item() {
        return BuiltInRegistries.ITEM.getValue(SPIRITUAL_STONE_ID);
    }

    public static boolean isAvailable() {
        Item item = item();
        return item != null && item != Items.AIR;
    }

    public static long count(ServerPlayer player) {
        Item currency = item();
        if (currency == null || currency == Items.AIR) {
            return 0L;
        }

        long total = 0L;
        for (int slot = 0; slot < Math.min(INVENTORY_SLOT_COUNT, player.getInventory().getContainerSize()); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(currency)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean remove(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (!isAvailable() || count(player) < amount) {
            return false;
        }

        long remaining = amount;
        for (int slot = 0; slot < Math.min(INVENTORY_SLOT_COUNT, player.getInventory().getContainerSize()) && remaining > 0L; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item())) {
                continue;
            }
            int take = (int) Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
        player.getInventory().setChanged();
        return remaining == 0L;
    }

    public static long give(ServerPlayer player, long amount) {
        Item currency = item();
        if (amount <= 0L || currency == null || currency == Items.AIR) {
            return 0L;
        }

        long remaining = amount;
        while (remaining > 0L) {
            int count = (int) Math.min(remaining, new ItemStack(currency).getMaxStackSize());
            ItemStack stack = new ItemStack(currency, count);
            int before = stack.getCount();
            player.getInventory().add(stack);
            int inserted = before - stack.getCount();
            if (inserted <= 0) {
                break;
            }
            remaining -= inserted;
        }
        player.getInventory().setChanged();
        return amount - remaining;
    }
}
