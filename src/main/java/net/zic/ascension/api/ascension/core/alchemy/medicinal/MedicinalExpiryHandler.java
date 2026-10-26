package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;

/**
 * Clears expired medicine from loaded player inventories at a low, staggered frequency.
 * Unloaded or nested inventories remain untouched until accessed, with consumption always validated.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MedicinalExpiryHandler {
    private MedicinalExpiryHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = MedicinalDecay.now(player.level());
        if (Math.floorMod(now + player.getId(), 200) != 0) return;
        var inventory = player.getInventory();
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty() && MedicinalDecay.expired(stack, now)) {
                inventory.setItem(index, MedicinalDecay.toResidue(stack));
            }
        }
    }
}
