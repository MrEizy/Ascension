package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.rpg_engine.source.OriginSourceEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class MobEventHandler {
    /*
        TODO:
            remove initialize on entity, then handle everything in on join.
            everything should be as deterministic as possible
            so for a path, if they have a path. do nothing, if not generate fresh path
     */

    @SubscribeEvent
    public static void onEntitySpawn(FinalizeSpawnEvent event){
        MobSpawnHelper.buildConfigurationInstance(event.getEntity());
    }
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event){

        if(!(event.getEntity() instanceof Mob mob)) return;
        MobSpawnHelper.applyConfigurationInstance(mob);

    }

    @SubscribeEvent
    public static void onSourceLoad(OriginSourceEvent.OriginSourceFinishedLoadingEvent event){
        for(LivingEntity attachedEntity : event.getSource().getAttachedEntities()){
            if(!attachedEntity.hasData(AscensionAttachments.MOB_CONFIG_HOLDER)) continue;

            MobConfigurationHolder holder = attachedEntity.getData(AscensionAttachments.MOB_CONFIG_HOLDER);

            AscensionEntityDataProvider provider = attachedEntity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

            if(provider == null) continue;
            holder.applyConfigurationToSource();
        }
    }
}
