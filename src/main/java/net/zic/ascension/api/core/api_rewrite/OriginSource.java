package net.zic.ascension.api.core.api_rewrite;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.stats.StatSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO add interface stat sheet provider
public class OriginSource {
    private final HashMap<Identifier, DataSourceInstance> dataSources = new HashMap<>();

    private ValueInput cachedData; //used in situations where we cannot easily have registry access

    private final StatSheet statSheet = new StatSheet();

    //cheating a bit here
    public RegistryAccess getRegistryAccess(){
        return Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().registryAccess();
    }


    //──Source State────────────────────────────────────────────────────────
    //used to handle snapshot syncing, a snapshot will be created when a process is started
    //and is only sent when a matching end process is triggered
    public void startProcess(String processId){}
    public void endProcess(String processId){}

    public void markDataSourceDirty(Identifier source){} //tells the snapshot to sync this data source instance



    //──Data Sources────────────────────────────────────────────────────────

    public boolean addDataSource(Identifier dataSource,DataSourceInstance instance){
        if(dataSources.containsKey(dataSource)) return false;
        dataSources.put(dataSource,instance);
        instance.getDataSource().onAdded(this,instance);
        return true;
    }
    public boolean hasDataSource(Identifier dataSource){
        return dataSources.containsKey(dataSource);
    }
    public DataSourceInstance getDataSource(Identifier dataSource){
        return dataSources.get(dataSource);
    }
    public DataSourceInstance removeDataSource(Identifier dataSource){
        return dataSources.remove(dataSource);
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
