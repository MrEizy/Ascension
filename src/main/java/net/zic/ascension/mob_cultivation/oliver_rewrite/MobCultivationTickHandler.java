package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationTickHandler {
    private MobCultivationTickHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob) {
            MobCultivationProgressionService.tick(mob);
        }
    }
}
