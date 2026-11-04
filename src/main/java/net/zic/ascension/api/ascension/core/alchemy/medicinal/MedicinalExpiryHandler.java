package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.menus.jade_bottle.JadeBottleMenu;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MedicinalExpiryHandler {
    private MedicinalExpiryHandler() {
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MedicinalContainerAccess.reconcileMenu(event.getContainer(), player, MedicinalDecay.now(player.level()));
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MedicinalContainerAccess.reconcileMenu(event.getContainer(), player, MedicinalDecay.now(player.level()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = MedicinalDecay.now(player.level());
        if (Math.floorMod(now + player.getId(), 200) != 0) return;

        boolean bottleOpen = player.containerMenu instanceof JadeBottleMenu;
        var inventory = player.getInventory();
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            ItemStack original = inventory.getItem(index);
            ItemStack resolved = bottleOpen && !MedicinalDecay.expired(original, now)
                    ? original : MedicinalContainerAccess.reconcile(original, now);
            if (resolved != original) inventory.setItem(index, resolved);
        }
        if (player.containerMenu != player.inventoryMenu) {
            MedicinalContainerAccess.reconcileMenu(player.containerMenu, player, now);
        }
    }
}
