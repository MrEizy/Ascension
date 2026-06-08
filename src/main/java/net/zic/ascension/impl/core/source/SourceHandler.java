package net.zic.ascension.impl.core.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.*;

//TODO this needs to be massively redone in the future
//TODO internally store an extra map of UUID -> source, this is what remote listeners use to get their source on load
//TODO i need a way to store BOTH loaded and unloaded listeners, then add a change watcher state instead of removing them on leaving a level
//TODO this way implementations can check how many watchers there are
//TODO so to clarify, when leaving/changing dimensions/respawn, we change state to offline, if they rejoin/load we change to online
//TODO for non player entities when they die we REMOVE their listener instead of changing to offline
//TODO listeners can then manually change this for themselves

//TODO need to get the rest of the mod in a good state so i can test this
@EventBusSubscriber
public class SourceHandler extends SavedData {

    private final HashMap<UUID,OriginSource> remoteSources = new HashMap<>();
    private final HashMap<OriginSource,UUID> sourceIdMap = new HashMap<>();
    private final HashMap<OriginSource,HashSet<SourceWatcher>> sourceWatchers = new HashMap<>();
    private final HashMap<SourceWatcher,OriginSource> watchers = new HashMap<>();
    private final HashMap<UUID,SourceWatcher> watchersIdMap = new HashMap<>();


    public static final Codec<OriginSource> ORIGIN_CODEC = Codec.of(new SourceEncoder(),new SourceDecoder());
    public static final SavedDataType<SourceHandler> ID = new SavedDataType<>(

            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "source_handler"),
            SourceHandler::new,
            level -> RecordCodecBuilder.create(instance -> instance.group(
                    RecordCodecBuilder.point(level),

                    Codec.unboundedMap(
                                    UUIDUtil.CODEC,
                                    ORIGIN_CODEC
                            ).fieldOf("remote_sources")
                            .forGetter(SourceHandler::getRemoteSources),

                    Codec.unboundedMap(
                                    UUIDUtil.CODEC,
                                    UUIDUtil.CODEC
                            ).fieldOf("watchers")
                            .forGetter(SourceHandler::getWatchers)
            ).apply(instance, SourceHandler::new))
    );


    public SourceHandler(ServerLevel level){

    }
    public SourceHandler(ServerLevel level, Map<UUID,OriginSource> remoteSources, Map<UUID,UUID> watchers){
        //TODO implement and load
    }

    public Map<UUID,OriginSource> getRemoteSources(){
        return Map.copyOf(remoteSources);
    }
    public Map<UUID,UUID> getWatchers(){
        HashMap<UUID,UUID> map = new HashMap<>();
        watchers.forEach((watcher,source)->{
            if(sourceIdMap.containsKey(source)) map.put(watcher.getUuid(),sourceIdMap.get(source));
        });
        return map;
    }

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
        if(watcher == null) return;
        if(sourceWatchers.get(watchers.get(watcher)).size() == 1){
            removeWatcher(entity);
            return;
        }

        watcher.setEntity(null);
    }
    public void updateWatcherEntity(LivingEntity entity){
        if(watchersIdMap.containsKey(entity.getUUID())) watchersIdMap.get(entity.getUUID()).setEntity(entity);
        System.out.println(watchersIdMap.get(entity.getUUID()).isLoaded());
    }

    public void addWatcher(LivingEntity entity,OriginSource source){

        if(!sourceWatchers.containsKey(source)){
            sourceWatchers.put(source,new HashSet<>());
        }
        if(watchersIdMap.containsKey(entity.getUUID())){
            changeWatcherState(entity,true);
            updateWatcherEntity(entity);
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
        if(sourceWatchers.get(source).isEmpty()) sourceWatchers.remove(source);
    }

    public Collection<LivingEntity> getLoadedWatchers(OriginSource source){
        if(!sourceWatchers.containsKey(source)) return List.of();
        ArrayList<LivingEntity> arrayList = new ArrayList<>();
        for(SourceWatcher watcher : sourceWatchers.get(source)){
            if(watcher.isLoaded()) arrayList.add(watcher.getEntity());
        }
        return arrayList;
    }

    public void applyToWatcher(LivingEntity watcher){
        if(!watchersIdMap.containsKey(watcher.getUUID())) return;
        OriginSource source = watchers.get(watchersIdMap.get(watcher.getUUID()));
        Physique physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,source.getPhysique(),source.getRegistryAccess());
        if(physique != null){
            physique.applyToEntity(watcher,source.getPhysiqueData());
        }
        for(Identifier bloodline : source.getBloodlines()){
            Bloodline bloodlineInstance = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,source.getRegistryAccess());
            if(bloodlineInstance != null){
                bloodlineInstance.applyToEntity(watcher,source.getBloodlineData(bloodline));
            }
        }
        for(Identifier skill: source.getSkills()){
            Skill skillInstance = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,source.getRegistryAccess());
            if(skillInstance != null){
                skillInstance.applyToEntity(watcher,source.getSkillData(skill));
            }
        }

    }
    public void removeFromWatcher(LivingEntity watcher){
        if(!watchersIdMap.containsKey(watcher.getUUID())) return;
        OriginSource source = watchers.get(watchersIdMap.get(watcher.getUUID()));
        Physique physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,source.getPhysique(),source.getRegistryAccess());
        if(physique != null){
            physique.removeFromEntity(watcher,source.getPhysiqueData());
        }
        for(Identifier bloodline : source.getBloodlines()){
            Bloodline bloodlineInstance = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,source.getRegistryAccess());
            if(bloodlineInstance != null){
                bloodlineInstance.removeFromEntity(watcher,source.getBloodlineData(bloodline));
            }
        }
        for(Identifier skill: source.getSkills()){
            Skill skillInstance = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,source.getRegistryAccess());
            if(skillInstance != null){
                skillInstance.removeFromEntity(watcher,source.getSkillData(skill));
            }
        }
    }
    /**TODO
     * THE PROCESS, ON ENTITY LOAD FIRST INITIALIZE THE ENTITY,
     * THEN ADD IT AS A WATCHER
     */
    //TODO get save states working
    //TODO register static event listeners, that Reach to AscensionCraft.SourceHandler

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        AscensionEntityDataHolder holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;

        holder.getData(event.getEntity()).initialize();
    }
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        AscensionCraft.getSourceHandler().changeWatcherState(event.getEntity(),false);
    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
        AscensionEntityDataHolder holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;

        AscensionCraft.getSourceHandler().addWatcher(event.getEntity(),holder.getData(event.getEntity()).getSource());

        holder.getData(event.getEntity()).initialize();

    }
}
