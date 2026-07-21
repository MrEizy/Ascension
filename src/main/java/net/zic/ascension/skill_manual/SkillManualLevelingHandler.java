package net.zic.ascension.skill_manual;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.data_source.DataSourceInstance;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.skill_casting.SkillCastEvent;

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
        AscensionEntityDataProvider holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
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
