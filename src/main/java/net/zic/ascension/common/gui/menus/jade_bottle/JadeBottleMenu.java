package net.zic.ascension.common.gui.menus.jade_bottle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.zic.ascension.common.gui.menus.AscMenuTypes;
import net.zic.ascension.common.util.ModTags;

public class JadeBottleMenu extends AbstractContainerMenu {
    private static final int PILL_SLOT_X = 80;
    private static final int PILL_SLOT_Y = 34;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_ROW_Y = 70;
    private static final int PLAYER_INV_HOTBAR_Y = 128;

    private final SimpleContainer pillContainer = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return PillSlot.MAX_STACK;
        }
    };
    private final ItemStack bottleStack;

    // Server-side construction (from JadeBottleItem#use)
    public JadeBottleMenu(int containerId, Inventory playerInventory, ItemStack bottleStack) {
        super(AscMenuTypes.JADE_BOTTLE.get(), containerId);
        this.bottleStack = bottleStack;
        loadFromStack();

        addSlot(new PillSlot(pillContainer, 0, PILL_SLOT_X, PILL_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_ROW_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, PLAYER_INV_HOTBAR_Y));
        }
    }

    // Client-side construction (decoded from the IContainerFactory buf)
    public static JadeBottleMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
        return new JadeBottleMenu(containerId, playerInventory, stack);
    }

    private void loadFromStack() {
        ItemContainerContents contents = bottleStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.copyInto(pillContainer.getItems());
    }

    private void saveToStack() {
        bottleStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(pillContainer.getItems()));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        saveToStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == bottleStack || player.getOffhandItem() == bottleStack
                || player.getInventory().contains(bottleStack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (index == 0) {
            if (!moveItemStackTo(original, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (original.is(ModTags.Items.PILLS) && pillContainer.getItem(0).isEmpty()) {
                if (!moveItemStackTo(original, 0, 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
