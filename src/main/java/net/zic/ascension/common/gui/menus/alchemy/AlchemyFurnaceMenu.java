package net.zic.ascension.common.gui.menus.alchemy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;
import net.zic.ascension.common.gui.menus.AscMenuTypes;

public class AlchemyFurnaceMenu extends AbstractContainerMenu {
    private final AlchemyFurnaceBlockEntity furnace;
    private final Container container;
    private final ContainerData progress;

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof AlchemyFurnaceBlockEntity entity ? entity : null);
    }

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, AlchemyFurnaceBlockEntity furnace) {
        super(AscMenuTypes.ALCHEMY_FURNACE.get(), id);
        this.furnace = furnace;
        this.container = furnace == null ? new SimpleContainer(4) : furnace.inventory();
        this.progress = furnace == null ? new SimpleContainerData(2) : furnace.menuData();
        addSlot(new Slot(container, 0, 52, 20) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 1, 79, 10) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 2, 106, 20) {
            @Override public boolean mayPlace(ItemStack stack) { return editable() && AlchemyMaterials.resolve(stack, net.zic.ascension.api.ascension.core.alchemy.AlchemyContext.of(playerInventory.player)).isPresent(); }
            @Override public boolean mayPickup(Player player) { return editable(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 3, 97, 59) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 83 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 141));
        addDataSlots(progress);
    }

    private boolean editable() { return furnace == null || furnace.phase() == AlchemyFurnaceBlockEntity.Phase.IDLE; }
    public int phase() { return progress.get(0); }
    public int phaseTicks() { return progress.get(1); }
    @Override public boolean stillValid(Player player) {
        return furnace == null || (player.level().getBlockEntity(furnace.getBlockPos()) == furnace
                && player.distanceToSqr(furnace.getBlockPos().getX() + 0.5, furnace.getBlockPos().getY() + 0.5, furnace.getBlockPos().getZ() + 0.5) <= 64.0);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index == 3) {
            if (!moveItemStackTo(original, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index < 3) {
            if (!moveItemStackTo(original, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!editable() || !moveItemStackTo(original, 0, 3, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
