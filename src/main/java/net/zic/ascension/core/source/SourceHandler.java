package net.zic.ascension.core.source;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.common.AscensionAttachments;

import java.util.*;

//TODO this needs to be massively redone in the future
//TODO internally store an extra map of UUID -> source, this is what remote listeners use to get their source on load
//TODO i need a way to store BOTH loaded and unloaded listeners, then add a change watcher state instead of removing them on leaving a level
//TODO this way implementations can check how many watchers there are
//TODO so to clarify, when leaving/changing dimensions/respawn, we change state to offline, if they rejoin/load we change to online
//TODO for non player entities when they die we REMOVE their listener instead of changing to offline
//TODO listeners can then manually change this for themselves
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class SourceHandler {

    private static final HashMap<OriginSource, HashSet<LivingEntity>> sourceWatchers = new HashMap<>();

    private static final HashMap<LivingEntity,OriginSource> watchers = new HashMap<>();

    public static void addOriginSource(OriginSource source){
        if(sourceWatchers.containsKey(source)) return;
        sourceWatchers.put(source,new HashSet<>());
    }
    public static void addWatcher(LivingEntity watcher){
        if(watchers.containsKey(watcher)) return;
        AscensionEntityDataHolder holder = watcher.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;
        OriginSource source = holder.getData(watcher).getSource();
        addWatcher(watcher,source);

    }
    public static void addWatcher(LivingEntity watcher,OriginSource source){
        addOriginSource(source);
        sourceWatchers.get(source).add(watcher);
        watchers.put(watcher,source);
    }
    public static void removeWatcher(LivingEntity watcher,OriginSource source){
        if(!sourceWatchers.containsKey(source))return;
        sourceWatchers.get(source).remove(watcher);
        watchers.remove(watcher);
        if(sourceWatchers.get(source).isEmpty()) sourceWatchers.remove(source);

    }
    public static void removeWatcher(LivingEntity entity){
        if(watchers.containsKey(entity)) removeWatcher(entity,watchers.get(entity));

    }

    public static Collection<LivingEntity> getLoadedWatchers(OriginSource source){
        if(!sourceWatchers.containsKey(source)) return Set.of();
        return sourceWatchers.get(source);
    }
    //when an origin source is modified you should always call updateSource so all watchers can sync themselves
    //To the client
    public static void updateSource(OriginSource source){
        Set<LivingEntity> entities = sourceWatchers.get(source);
        for(LivingEntity entity : entities){
            AscensionEntityDataHolder holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
            if(holder == null) continue;
            holder.markDirty(entity);
        }
    }
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event){
        if(event.getEntity().level().isClientSide()) return;
        if(event.getEntity() instanceof LivingEntity livingEntity) removeWatcher(livingEntity);

    }
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if(livingEntity.level().isClientSide()) return;
        AscensionEntityDataHolder holder = livingEntity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;
        holder.getData(livingEntity).initialize();
        addWatcher(livingEntity);
    }
}
