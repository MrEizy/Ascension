package net.zic.ascension.core.source;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.*;

//TODO this needs to be massively redone in the future
//TODO internally store an extra map of UUID -> source, this is what remote listeners use to get their source on load
//TODO i need a way to store BOTH loaded and unloaded listeners, then add a change watcher state instead of removing them on leaving a level
//TODO this way implementations can check how many watchers there are
//TODO so to clarify, when leaving/changing dimensions/respawn, we change state to offline, if they rejoin/load we change to online
//TODO for non player entities when they die we REMOVE their listener instead of changing to offline
//TODO listeners can then manually change this for themselves

public class SourceHandler extends SavedData {

    private final HashMap<UUID,OriginSource> remoteSources = new HashMap<>();
    private final HashMap<OriginSource,UUID> sourceIdMap = new HashMap<>();
    private final HashMap<OriginSource,HashSet<SourceWatcher>> sourceWatchers = new HashMap<>();
    private final HashMap<SourceWatcher,OriginSource> watchers = new HashMap<>();
    private final HashMap<UUID,SourceWatcher> watchersIdMap = new HashMap<>();

    private static final HashMap<OriginSource, HashSet<LivingEntity>> sourceWatchers2 = new HashMap<>();

    private static final HashMap<LivingEntity,OriginSource> watchers2 = new HashMap<>();



    public UUID getTrackedSourceUUID(OriginSource source){
        return sourceIdMap.get(source);
    }

    public UUID addTrackedSource(OriginSource source){
        UUID uuid = UUID.randomUUID();
        remoteSources.put(uuid,source);
        sourceIdMap.put(source,uuid);
        return uuid;
    }

    public OriginSource getTrackedSource(UUID uuid){
        return remoteSources.get(uuid);
    }

    public void removeTrackedSource(UUID uuid){
        OriginSource source = remoteSources.remove(uuid);
        sourceIdMap.remove(source);
        Set<SourceWatcher> watchers =  sourceWatchers.remove(source);
        for(SourceWatcher watcher : watchers) watchers.remove(watcher);
    }

    public boolean isWatcher(LivingEntity entity){
        return watchersIdMap.containsKey(entity.getUUID());
    }

    public void changeWatcherState(LivingEntity entity,boolean load){
        if(load){
            //change to online
            watchersIdMap.get(entity.getUUID()).setEntity(entity);
            return;
        }
        //change to offline, itf only listener remove
        SourceWatcher watcher = watchersIdMap.get(entity.getUUID());
        if(sourceWatchers.get(watchers.get(watcher)).size() == 1){
            removeWatcher(entity);
            return;
        }

        watcher.setEntity(null);
    }
    public void addWatcher(LivingEntity entity){
        AscensionEntityDataHolder holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;
        OriginSource source = holder.getData(entity).getSource();
        if(!sourceWatchers.containsKey(source)){
            sourceWatchers.put(source,new HashSet<>());
        }
        if(watchersIdMap.containsKey(entity.getUUID())){
            changeWatcherState(entity,true);
            return;
        }

        SourceWatcher watcher = new SourceWatcher(entity);
        watchersIdMap.put(entity.getUUID(),watcher);
        watchers.put(watcher,source);
        sourceWatchers.get(source).add(watcher);
    }

    public void removeWatcher(LivingEntity entity){
        if(!watchersIdMap.containsKey(entity.getUUID())) return;

        SourceWatcher watcher = watchersIdMap.remove(entity.getUUID());
        OriginSource source = watchers.remove(watcher);
        sourceWatchers.get(source).remove(watcher);
    }

    public static Collection<LivingEntity> getLoadedWatchers(OriginSource source){
        if(!sourceWatchers2.containsKey(source)) return Set.of();
        return sourceWatchers2.get(source);
    }
    //when an origin source is modified you should always call updateSource so all watchers can sync themselves
    //To the client
    public static void updateSource(OriginSource source){
        Set<LivingEntity> entities = sourceWatchers2.get(source);
        for(LivingEntity entity : entities){
            AscensionEntityDataHolder holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
            if(holder == null) continue;
            holder.markDirty(entity);
        }
    }

    /**TODO
     * THE PROCESS, ON ENTITY LOAD FIRST INITIALIZE THE ENTITY,
     * THEN ADD IT AS A WATCHER
     */


    //TODO get save states working
    //TODO register static event listeners, that Reach to AscensionCraft.SourceHandler

}
