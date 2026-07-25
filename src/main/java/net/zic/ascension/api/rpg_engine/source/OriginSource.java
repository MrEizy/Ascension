package net.zic.ascension.api.rpg_engine.source;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.stats.event.StatsUpdatedEvent;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;
import java.util.stream.Collectors;


public class OriginSource implements StatProvider {
    private final HashMap<Identifier, DataSourceInstance> dataSources = new HashMap<>();

    private final HashSet<Identifier> dirtyDataSources = new HashSet<>();
    private final HashSet<Identifier> removedDataSources = new HashSet<>();

    private ValueInput cachedData; //used in situations where we cannot easily have registry access

    private final StatSheet statSheet = new StatSheet();

    private final HashSet<Stat> dirtyStats = new HashSet<>();

    private final HashSet<LivingEntity> attachedEntities = new HashSet<>();

    private String process;

    //cheating a bit here
    public RegistryAccess getRegistryAccess(){
        return Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().registryAccess();
    }


    //──Source State────────────────────────────────────────────────────────
    //used to handle snapshot syncing, a snapshot will be created when a process is started
    //and is only sent when a matching end process is triggered
    public void startProcess(String processId){
        if(process != null) return;
        process = processId;
    }
    protected OriginSourcePatch resolvePatch(){
        OriginSourcePatch patch =new OriginSourcePatch(
                dataSources.entrySet().stream().filter(entry->dirtyDataSources.contains(entry.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )),
                Set.copyOf(removedDataSources),
                statSheet.getAllInstances().stream().filter(instance->dirtyStats.contains(instance.getStat())).toList()
        );
        dirtyDataSources.clear();
        removedDataSources.clear();
        updateEntityStatHolder();
    }
    public boolean resolveProcess(String processId){
        return processId.equals(process);

    }

    //tells the snapshot to sync this data source instance
    public void markDataSourceDirty(Identifier source){
        dirtyDataSources.add(source);
    }

    public void attachToEntity(LivingEntity entity){
        attachedEntities.add(entity);

        for (DataSourceInstance instance : dataSources.values()) instance.getDataSource().applyToEntity(entity,instance);

        entity.getData(ZenithAttachments.STAT_HOLDER).registerStatProvider(this);
    }
    public void detachFromEntity(LivingEntity entity){
        attachedEntities.remove(entity);

        for (DataSourceInstance instance : dataSources.values()) instance.getDataSource().removeFromEntity(entity,instance);

        entity.getData(ZenithAttachments.STAT_HOLDER).removeStatProvider(this);
    }

    public Collection<LivingEntity> getAttachedEntities(){
        return attachedEntities;
    }
    public boolean isAttachedTo(LivingEntity entity){
        return attachedEntities.contains(entity);
    }
    //──Data Sources────────────────────────────────────────────────────────

    public boolean addDataSource(Identifier dataSource, DataSourceInstance instance){
        if(dataSources.containsKey(dataSource)) return false;
        dataSources.put(dataSource,instance);
        instance.getDataSource().onAdded(this,instance);
        markDataSourceDirty(dataSource);
        return true;
    }

    public boolean hasDataSource(Identifier dataSource){
        return dataSources.containsKey(dataSource);
    }

    public DataSourceInstance getDataSource(Identifier dataSource){
        return dataSources.get(dataSource);
    }

    public DataSourceInstance removeDataSource(Identifier dataSource){
        if(!dataSources.containsKey(dataSource)) return null;
        dataSources.get(dataSource).getDataSource().onRemoved(this,dataSources.get(dataSource));
        removedDataSources.add(dataSource);
        return dataSources.remove(dataSource);
    }

    //──Stat Sheet────────────────────────────────────────────────────────
    //TODO add methods for adding stats and multipliers


    public void addStat(Stat stat, double val){
        statSheet.addStat(stat,val);
        dirtyStats.add(stat);
    }

    public void removeStat(Stat stat, double val){
        addStat(stat,-val);
    }

    public void addStatModifier(Stat stat, ValueContainerModifier modifier){
        statSheet.addStat(stat,0); //makes sure the stat is present
        statSheet.getStatInstance(stat).addModifier(modifier);
        dirtyStats.add(stat);
    }
    public void removeStatModifier(Stat stat,Identifier identifier){
        if(statSheet.getStatInstance(stat) == null) return;
        statSheet.getStatInstance(stat).removeModifier(identifier);
        dirtyStats.add(stat);
    }

    public void updateEntityStatHolder(){
        if(dirtyStats.isEmpty()) return;
        for(LivingEntity entity : getAttachedEntities()){
            NeoForge.EVENT_BUS.post(new StatsUpdatedEvent(entity,dirtyStats));
        }
        dirtyStats.clear();
    }

    @Override
    public Collection<Stat> getStats() {
        return statSheet.getAllStats();
    }

    @Override
    public StatInstance getStatInstance(Stat stat) {
        return statSheet.getStatInstance(stat);
    }

    @Override
    public double getStat(Stat stat) {
        return getStatInstance(stat) != null? getStatInstance(stat).getValue():0;
    }

    @Override
    public double getBaseStat(Stat stat) {
        return getStatInstance(stat) != null? getStatInstance(stat).getBaseValue():0;
    }
    //──Source Data────────────────────────────────────────────────────────

    private static final Identifier NONE_IDENTIFIER = Identifier.parse("none");

    private DataSourceInstance loadDataSource(ValueInput input,RegistryAccess access){
        Identifier identifier = Identifier.parse(input.getStringOr("source_id","none"));

        if(identifier.equals(NONE_IDENTIFIER)){
            AscensionCraft.LOGGER.debug("tried loading non-existent data source");
            return null;
        }

        try{
            return DataSource.getInstance(identifier).loadInstance(input.childOrEmpty("data"),access);
        }catch (Exception e){
            AscensionCraft.LOGGER.debug("error trying to load data source {}",identifier);
        }
        return null;
    }

    private void writeDataSource(Identifier dataSource, DataSourceInstance instance, ValueOutput output, RegistryAccess access){
        if(instance == null) return;
        output.putString("source_id",dataSource.toString());
        instance.getDataSource().writeInstance(instance,output.child("data"),access);
    }

    void loadOriginSourceData(ValueInput input){
        HashMap<LoadPriority,ArrayList<DataSourceInstance>> loadMap = new HashMap<>();

        ValueInput.ValueInputList inputList = input.childrenListOrEmpty("data_sources");
        for(ValueInput dataSourceInput : inputList){
            DataSourceInstance instance = loadDataSource(dataSourceInput,getRegistryAccess());
            if(instance == null) continue;

            loadMap.computeIfAbsent(instance.getDataSource().loadPriority(),key->new ArrayList<>());

            loadMap.get(instance.getDataSource().loadPriority()).add(instance);
        }

        for(LoadPriority priority : LoadPriority.values()){
            if(!loadMap.containsKey(priority)) continue;
            List<DataSourceInstance> instances = loadMap.get(priority);

            for(DataSourceInstance instance : instances){
                if(!addDataSource(DataSource.getId(instance.getDataSource()),instance)) AscensionCraft.LOGGER.debug(
                        "unable to add data source {}",DataSource.getId(instance.getDataSource())
                );
            }
        }
        for(DataSourceInstance instance : dataSources.values()) instance.getDataSource().finishedLoading(this,instance);
    }

    void writeOriginSourceData(ValueOutput output){

        ValueOutput.ValueOutputList outputList = output.childrenList("data_sources");
        for(Identifier dataSource : dataSources.keySet()){
            writeDataSource(dataSource,dataSources.get(dataSource),outputList.addChild(),getRegistryAccess());
        }
    }


}
