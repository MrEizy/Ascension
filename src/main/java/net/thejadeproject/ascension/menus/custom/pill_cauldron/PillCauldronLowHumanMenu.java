package net.thejadeproject.ascension.menus.custom.pill_cauldron;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.entity.PillCauldronLowHumanEntity;
import net.thejadeproject.ascension.menus.ModMenuTypes;

/**
 * Menu for the Pill Cauldron.
 *
 * Slots 0-2 (mirror pedestal items) are DISPLAY ONLY – cannot be inserted into or taken from.
 * Slots 3-4 are the success/fail output slots.
 *
 * ContainerData:
 *   [0] progress  [1] maxProgress  [2] flameLit  [3] fanProgress  [4] needsFanning
 */
public class PillCauldronLowHumanMenu extends AbstractContainerMenu {

    public final PillCauldronLowHumanEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // ── Network constructor ───────────────────────────────────────
    public PillCauldronLowHumanMenu(int id, Inventory inv, FriendlyByteBuf extra) {
        this(id, inv, inv.player.level().getBlockEntity(extra.readBlockPos()), new SimpleContainerData(7));
    }

    // ── Direct constructor ────────────────────────────────────────
    public PillCauldronLowHumanMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.PILL_CAULDRON_LOW_HUMAN_MENU.get(), id);
        this.blockEntity = (PillCauldronLowHumanEntity) entity;
        this.level = inv.player.level();
        this.data  = data;

        addPlayerHotbar(inv);
        addPlayerInventory(inv);

        // ── Pedestal mirror slots (display only, locked) ──────────
        // Slot 0 → left pedestal  (west)   displayed at left input position
        this.addSlot(new LockedSlot(blockEntity.itemHandler, 0, 53, 20));
        // Slot 1 → top pedestal   (north)  displayed at top input position
        this.addSlot(new LockedSlot(blockEntity.itemHandler, 1, 80, 9));
        // Slot 2 → right pedestal (east)   displayed at right input position
        this.addSlot(new LockedSlot(blockEntity.itemHandler, 2, 107, 20));

        // ── Output slots ──────────────────────────────────────────
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 3, 62, 59));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 4, 98, 59));

        addDataSlots(data);
    }

    // ── ContainerData helpers ─────────────────────────────────────
    // Slot layout: 0=progress, 1=maxProgress, 2=flameLit, 3=temperature, 4=minTemp, 5=maxTemp
    public boolean isCrafting()         { return data.get(0) > 0 && data.get(1) > 0; }
    public boolean isFlameStandLit()    { return data.get(2) == 1; }
    public int getCurrentTemp()         { return data.get(3); }
    public int getRecipeMinTemp()       { return data.get(4); }
    public int getRecipeMaxTemp()       { return data.get(5); }
    public int getRecipeIdealTemp()     { return data.get(6); }

    public int getScaledArrowProgress() {
        int prog = data.get(0);
        int max  = data.get(1);
        return max != 0 && prog != 0 ? prog * 21 / max : 0;
    }

    // ── Shift-click ───────────────────────────────────────────────
    private static final int HOTBAR_SLOTS      = 9;
    private static final int PLAYER_INV_ROWS   = 3;
    private static final int PLAYER_INV_COLS   = 9;
    private static final int VANILLA_SLOTS     = HOTBAR_SLOTS + PLAYER_INV_ROWS * PLAYER_INV_COLS;
    private static final int TE_FIRST          = VANILLA_SLOTS;
    private static final int TE_SLOTS          = 5;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < TE_FIRST) {
            // From player inventory → only output slots (3-4) accept shift-click in reverse
            // Mirror slots (0-2) cannot receive items
            if (!moveItemStackTo(sourceStack, TE_FIRST + 3, TE_FIRST + TE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            int teSlot = index - TE_FIRST;
            if (teSlot < 3) return ItemStack.EMPTY; // mirror slots – no shift-click
            // Output slots → player inventory
            if (!moveItemStackTo(sourceStack, 0, VANILLA_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == 0) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, sourceStack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.PILL_CAULDRON_HUMAN_LOW.get());
    }

    // ── Player slots ──────────────────────────────────────────────
    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    // ── Locked display slot ───────────────────────────────────────
    /**
     * A slot that cannot be interacted with by the player.
     * Used to show pedestal item mirrors without allowing insertion/removal.
     */
    private static class LockedSlot extends SlotItemHandler {
        public LockedSlot(net.neoforged.neoforge.items.IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) { return false; }

        @Override
        public boolean mayPickup(Player player) { return false; }
    }
}