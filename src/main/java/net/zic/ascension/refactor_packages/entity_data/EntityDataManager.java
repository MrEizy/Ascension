package net.zic.ascension.refactor_packages.entity_data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/*

    in order to realise if an entities data needs to be reatached i am going to do 1 of 2 things
    either find out how to modify the data component of unloaded entites directly, or wait till it is loaded,
    and check if there are any tethered entitites left


    in the current state it is still possible for desync to happen, mainly if a watcher has an active form that is removed by another

 */
public class EntityDataManager {



    private static HashMap<UUID,IEntityData> watchableEntityData = new HashMap<>();

    //holds an entity id and connects it to an ID that references an entityData
    private static HashMap<UUID,UUID> entityWatchers = new HashMap<>();

    //holds a set of all watchers for each UUID, if it ever gets to 1 and the watcher is loaded move onto watcher
    //the UUID in this scenario is the "original"
    private static HashMap<UUID, HashSet<UUID>> watchlist = new HashMap<>();

    public static void addWatchableEntityData(UUID entity,IEntityData entityData){
        watchableEntityData.put(entity,entityData);
        entityWatchers.put(entity,entity);
        watchlist.put(entity,new HashSet<>(){{add(entity);}});
    }

    public static void watchEntityData(UUID watcher, UUID entity){
        entityWatchers.put(watcher,entity);
        watchlist.get(entity).add(watcher);
    }
    public static void removeWatcher(UUID watcher){
        UUID entity = entityWatchers.remove(watcher);
        watchlist.get(entity).remove(watcher);
    }
    public static boolean isWatching(UUID entity){
        return entityWatchers.containsKey(entity);
    }
    public static UUID getWatchedEntityDataUUID(UUID watcher){
        return entityWatchers.get(watcher);
    }
    /*
        In scenarios like trying to possess a new body I recommend not using this method, and instead rely on
        ability to entity data moving behaviour, or at least ensure there is only 1 watcher
     */
    public static IEntityData removeWatchableEntityData(UUID entity){

        for(UUID watcher : watchlist.remove(entity)){
            entityWatchers.remove(watcher);
        }
        return watchableEntityData.remove(entity);
    }
    public static IEntityData getEntityData(UUID uuid){
        return watchableEntityData.get(entityWatchers.get(uuid));
    }




}
