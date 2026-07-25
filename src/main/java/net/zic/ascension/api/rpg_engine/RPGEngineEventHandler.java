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
    public static void onPreDamage(LivingDamageEvent.Pre event){

        RPGEngineDamageSource newSource;
        if(event.getSource() instanceof RPGEngineDamageSource rpgEngineDamageSource) newSource = rpgEngineDamageSource;
        else newSource = new RPGEngineDamageSource(event.getSource());

        RPGEngineGatherDamageTypesEvent damageTypesEvent = new RPGEngineGatherDamageTypesEvent(event.getEntity(),newSource);
        NeoForge.EVENT_BUS.post(damageTypesEvent);

        RPGEngineEntityDamagedEvent.Pre rpgEngineEntityDamagedEvent = new RPGEngineEntityDamagedEvent.Pre(event.getEntity(),event.getContainer());

        NeoForge.EVENT_BUS.post(rpgEngineEntityDamagedEvent);

        event.setNewDamage((float) rpgEngineEntityDamagedEvent.getDamage());

        /*TODO ideally replace source with new source
        but there seems to be issues with access transformers for neoforge classes
         */
        RPGEngineEntityDamagedEvent.Post rpgEngineEntityDamagedEventPose = new RPGEngineEntityDamagedEvent.Post(rpgEngineEntityDamagedEvent);
        NeoForge.EVENT_BUS.post(rpgEngineEntityDamagedEventPose);
    }

}
