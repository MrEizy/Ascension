package net.zic.ascension.impl.core.damage;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.core.skill.passive.PassiveDefenseService;
import net.zic.ascension.impl.runtime.object.Barriers;
import net.zic.ascension.impl.runtime.object.OwnerBoundConstructs;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class CombatPipeline {
    private CombatPipeline() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
        PassiveDefenseService.applyDamage(event);
        if (event.getDamage() <= 0.0D) {
            return;
        }
        OwnerBoundConstructs.applyDamage(event);
        if (event.getDamage() <= 0.0D) {
            return;
        }
        Barriers.applyDamage(event);
    }
}
