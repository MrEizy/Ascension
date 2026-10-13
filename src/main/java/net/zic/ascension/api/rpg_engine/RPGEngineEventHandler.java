package net.zic.ascension.api.rpg_engine;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineGatherDamageTypesEvent;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class RPGEngineEventHandler {

    @SubscribeEvent
    public static void onPreDamage(LivingDamageEvent.Pre event) {
        RPGEngineDamageSource source = resolveSource(event.getSource());
        NeoForge.EVENT_BUS.post(new RPGEngineGatherDamageTypesEvent(event.getEntity(), source));

        RPGEngineEntityDamagedEvent.Pre pre = new RPGEngineEntityDamagedEvent.Pre(event.getEntity(), event.getContainer(), source);
        NeoForge.EVENT_BUS.post(pre);

        double resolved = pre.getDamage();
        event.setNewDamage(Double.isFinite(resolved) ? (float) Math.clamp(resolved, 0.0D, Float.MAX_VALUE) : 0.0F);
    }

    @SubscribeEvent
    public static void onPostDamage(LivingDamageEvent.Post event) {
        RPGEngineDamageSource source = resolveSource(event.getSource());
        NeoForge.EVENT_BUS.post(new RPGEngineGatherDamageTypesEvent(event.getEntity(), source));
        NeoForge.EVENT_BUS.post(new RPGEngineEntityDamagedEvent.Post(event, source));
    }

    private static RPGEngineDamageSource resolveSource(net.minecraft.world.damagesource.DamageSource source) {
        return source instanceof RPGEngineDamageSource rpgSource ? rpgSource : new RPGEngineDamageSource(source);
    }
}
