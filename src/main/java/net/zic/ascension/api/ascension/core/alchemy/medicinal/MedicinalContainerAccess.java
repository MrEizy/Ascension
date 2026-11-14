package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public final class MedicinalContainerAccess {
    private static final int MAX_NESTING = 4;
    private static final int MAX_VISITED = 512;

    private MedicinalContainerAccess() {
    }

    public static ItemStack reconcile(ItemStack stack, long now) {
        return reconcile(stack, now, 0, new ScanBudget());
    }

    private static ItemStack reconcile(ItemStack stack, long now, int depth, ScanBudget budget) {
        if (stack.isEmpty() || budget.remaining-- <= 0) return stack;
        if (MedicinalDecay.expired(stack, now)) return MedicinalDecay.toResidue(stack);
        if (depth >= MAX_NESTING) return stack;

        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) return stack;

        List<ItemStack> items = new ArrayList<>(contents.allItemsCopyStream().toList());
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack original = items.get(i);
            if (budget.remaining <= 0) break;
            ItemStack resolved = reconcile(original, now, depth + 1, budget);
            if (resolved != original) {
                items.set(i, resolved);
                changed = true;
            }
        }
        if (!changed) return stack;

        ItemStack updated = stack.copy();
        updated.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        return updated;
    }

    private static final class ScanBudget {
        private int remaining = MAX_VISITED;
    }

    public static void reconcileMenu(AbstractContainerMenu menu, ServerPlayer player, long now) {
        boolean changed = false;
        for (Slot slot : menu.slots) {
            if (slot.container == player.getInventory() || slot.container instanceof ResultContainer) continue;
            ItemStack original = slot.getItem();
            ItemStack resolved = reconcile(original, now);
            if (resolved != original) {
                slot.set(resolved);
                changed = true;
            }
        }
        if (changed) menu.broadcastChanges();
    }
}
