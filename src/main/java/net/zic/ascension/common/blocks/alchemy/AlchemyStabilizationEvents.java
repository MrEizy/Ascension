package net.zic.ascension.common.blocks.alchemy;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AlchemyStabilizationEvents {
    private AlchemyStabilizationEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof AlchemyFurnaceBlockEntity furnace
                && furnace.phase() == AlchemyFurnaceBlockEntity.Phase.STABILIZATION
                && event.getEntity().getUUID().equals(furnace.alchemistId())) {
            event.setCanceled(true);
        }
    }
}
