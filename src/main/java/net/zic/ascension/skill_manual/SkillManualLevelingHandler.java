package net.zic.ascension.skill_manual;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.skill_casting.SkillCastEvent;

import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;

@EventBusSubscriber
public class SkillManualLevelingHandler {
    private static final HashSet<SkillManualSource> holderSources = new HashSet<>();

    public static void addHolder(SkillManualSource source){
        holderSources.add(source);
    }

    @SubscribeEvent
    private static void onServerStop(ServerStoppedEvent event){
        holderSources.clear();
    }

    @SubscribeEvent
    private static void onSkillCast(SkillCastEvent event){
        AscensionEntityDataHolder holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;
        AscensionEntityData data = holder.getData(event.getEntity());
        if(data == null) return;

        for(SkillManualSource source : holderSources){
            Identifier key = CoreRegistries.DATA_SOURCE_REGISTRY.get(event.getEntity().level().registryAccess()).getKey(source);

            if(key == null) continue;

            DataSourceInstance instance =data.getSource().getDataSourceInstance(key);

            if(!(instance instanceof SkillManualHolder manualHolder)) continue;


            manualHolder.skillCast(event.getEntity(),event.getSkill());

        }
    }

}
