package net.zic.ascension.common.gui.menus.jade_bottle;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalDecay;
import net.zic.ascension.common.util.ModTags;

public class PillSlot extends Slot {
    public static final int MAX_STACK = 8;
    private final Level level;

    public PillSlot(Container container, int index, int x, int y, Level level) {
        super(container, index, x, y);
        this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModTags.Items.PILLS)
                && !MedicinalDecay.expired(stack, MedicinalDecay.now(level));
    }

    @Override
    public void set(ItemStack stack) {
        if (!level.isClientSide()) {
            MedicinalDecay.transition(stack, MedicinalDecay.now(level), MedicinalDecay.jadeRate());
        }
        super.set(stack);
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack taken = super.remove(amount);
        if (!level.isClientSide()) {
            MedicinalDecay.transition(taken, MedicinalDecay.now(level), MedicinalDecay.OPEN_AIR_RATE);
        }
        return taken;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!level.isClientSide()) {
            MedicinalDecay.transition(stack, MedicinalDecay.now(level), MedicinalDecay.OPEN_AIR_RATE);
        }
        super.onTake(player, stack);
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK;
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return MAX_STACK;
    }
}
