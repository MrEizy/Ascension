package net.thejadeproject.ascension.common.items.artifacts;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class EnderPouch extends Item {

    // Track which players have the Ender Pouch GUI open
    private static final Set<UUID> activeUsers = new HashSet<>();

    public EnderPouch(Properties properties) {
        super(properties.stacksTo(1));
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            activeUsers.add(player.getUUID());

            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) ->
                            new LockedEnderChestMenu(containerId, playerInventory, player.getEnderChestInventory(), this),
                    Component.translatable("item.ascension.ender_pouch_gui")
            ));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS,
                    0.5F, level.random.nextFloat() * 0.1F + 0.9F);

            player.getCooldowns().addCooldown(this, 50);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        activeUsers.remove(player.getUUID());
    }

    public static boolean isPlayerUsingEnderPouch(Player player) {
        return activeUsers.contains(player.getUUID());
    }

    public static class LockedEnderChestMenu extends ChestMenu {
        private final EnderPouch enderPouchItem;

        public LockedEnderChestMenu(int containerId, Inventory playerInventory, Container enderChestInventory, EnderPouch enderPouchItem) {
            super(MenuType.GENERIC_9x3, containerId, playerInventory, enderChestInventory, 3);
            this.enderPouchItem = enderPouchItem;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            Slot slot = this.slots.get(index);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (stack.getItem() == enderPouchItem) {
                    if (isPlayerUsingEnderPouch(player)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            return super.quickMoveStack(player, index);
        }

        @Override
        public void clicked(int slotId, int button, ClickType clickType, Player player) {
            if (slotId >= 0 && slotId < this.slots.size()) {
                Slot slot = this.slots.get(slotId);
                if (slot != null && slot.hasItem()) {
                    ItemStack stack = slot.getItem();
                    if (stack.getItem() == enderPouchItem && isPlayerUsingEnderPouch(player)) {
                        return;
                    }
                }
            }
            super.clicked(slotId, button, clickType, player);
        }
    }
}