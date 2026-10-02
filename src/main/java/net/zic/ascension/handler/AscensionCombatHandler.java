package net.zic.ascension.handler;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.runtime.projectile.NormalProjectileService;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AscensionCombatHandler {
    private AscensionCombatHandler() {
    }

    @SubscribeEvent
    public static void onRPGEngineDamage(RPGEngineEntityDamagedEvent.Post event) {
        NormalProjectileService.handleDamagePost(event);
    }
}
