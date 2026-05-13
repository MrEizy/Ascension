package net.thejadeproject.ascension.menus.custom.spirit_ring;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.thejadeproject.ascension.common.items.artifacts.SpatialRing;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.menus.ModMenuTypes;

public class SpatialRingInventoryMenu extends AbstractContainerMenu {
    private ItemStack stack;
    private ItemStackHandler handler;
    private Inventory inventory;

    private static final int PLAYER_INV_START = 0;
    private static final int PLAYER_INV_END   = 27;
    private static final int HOTBAR_START      = 27;
    private static final int HOTBAR_END        = 36;
    private final int ringStart;
    private final int ringEnd;

    public SpatialRingInventoryMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SPATIAL_RING_INVENTORY_MENU.get(),containerId);
        this.inventory = inventory;
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        this.stack = inventory.player.getItemInHand(InteractionHand.MAIN_HAND);
        this.handler = stack.get(ModDataComponents.SPIRIT_RING_DATA).createItemHandler(stack);
        this.ringStart = HOTBAR_END;
        for(int i = 0; i<handler.getSlots();i++){
            this.addSlot(new SlotItemHandler(handler,i,0,0){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !(stack.getItem() instanceof SpatialRing) && super.mayPlace(stack);
                }
            });
        }
        this.ringEnd = ringStart + handler.getSlots();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 0, 0));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i,0, 0));
        }
    }

    public int getSpatialRingInventorySlots(){
        return handler.getSlots();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        boolean fromRing   = index >= ringStart && index < ringEnd;
        boolean fromInv    = index >= PLAYER_INV_START && index < PLAYER_INV_END;
        boolean fromHotbar = index >= HOTBAR_START && index < HOTBAR_END;

        if (fromRing) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (fromInv) {
            if (stack.getItem() instanceof SpatialRing || !moveItemStackTo(stack, ringStart, ringEnd, false)) {
                if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (fromHotbar) {
            if (stack.getItem() instanceof SpatialRing || !moveItemStackTo(stack, ringStart, ringEnd, false)) {
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).equals(stack);
    }

}