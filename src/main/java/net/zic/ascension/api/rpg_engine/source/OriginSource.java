package net.zic.ascension.api.rpg_engine.source;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceHolder;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.*;
import java.util.stream.Collectors;

//TODO ensure that when adding new Data sources they properly have add to entity called
//TODO then do the same for stuff like physique bloodline etc
public class OriginSource implements StatProvider {
    private final HashMap<Identifier, DataSourceHolder<?>> dataSources = new HashMap<>();

    private final HashSet<Identifier> dirtyDataSources = new HashSet<>();
    private final HashSet<Identifier> removedDataSources = new HashSet<>();

    private ValueInput cachedData; //used in situations where we cannot easily have registry access

    private final StatSheet statSheet = new StatSheet();

    private final HashSet<Stat> dirtyStats = new HashSet<>();

    private final HashSet<LivingEntity> attachedEntities = new HashSet<>();

    private String process;

    public RegistryAccess getRegistryAccess(){
        return ServerLifecycleHooks.getCurrentServer() == null
                ? (Minecraft.getInstance().getConnection() == null ? null : Minecraft.getInstance().getConnection().registryAccess())
                : ServerLifecycleHooks.getCurrentServer().registryAccess();
    }

    public void setCachedData(ValueInput cachedData){this.cachedData =cachedData;}

    //──Source State────────────────────────────────────────────────────────
    //used to handle snapshot syncing, a snapshot will be created when a process is started
    //and is only sent when a matching end process is triggered
    public void startProcess(String processId){
        if(process != null) return;
        process = processId;
    }
    public OriginSourcePatch resolvePatch(){
        OriginSourcePatch patch =new OriginSourcePatch(
                dataSources.entrySet().stream().filter(entry->dirtyDataSources.contains(entry.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )),
                Set.copyOf(removedDataSources),
                statSheet.getAllInstances().stream().filter(instance->dirtyStats.contains(
                        ZenithStatHelper.stat(instance)
                )).toList()
        );
        dirtyDataSources.clear();
        removedDataSources.clear();
        updateEntityStatHolder();
        return patch;
    }
    public OriginSourcePatch resolveFullPatch(){
        OriginSourcePatch patch = new OriginSourcePatch(
                Map.copyOf(dataSources),
                List.of(),
                statSheet.getAllInstances()
        );

        dirtyDataSources.clear();
        removedDataSources.clear();
        updateEntityStatHolder();
        return patch;
    }
    public boolean resolveProcess(String processId){
        if (!processId.equals(process)) {
            return false;
        }
        process = null;
        return true;
    }

    //tells the snapshot to sync this data source instance
    public void markDataSourceDirty(Identifier source){
        dirtyDataSources.add(source);
    }

    public void attachToEntity(LivingEntity entity) {
        if (entity == null || attachedEntities.contains(entity)) { return; }
        attachedEntities.add(entity);
        entity.getData(ZenithAttachments.STAT_HOLDER).registerStatProvider(this);
        for (DataSourceHolder<?> instance : dataSources.values()) { instance.applyToEntity(entity); }
    }
    public void detachFromEntity(LivingEntity entity){
        if(!attachedEntities.contains(entity)) return;
        attachedEntities.remove(entity);

        for (DataSourceHolder<?> instance : dataSources.values()) { instance.removeFromEntity(entity); }

        entity.getData(ZenithAttachments.STAT_HOLDER).removeStatProvider(this);
    }

    public Collection<LivingEntity> getAttachedEntities(){
        return attachedEntities;
    }
    public boolean isAttachedTo(LivingEntity entity){
        return attachedEntities.contains(entity);
    }
    //──Data Sources────────────────────────────────────────────────────────
    public boolean addDataSource(Identifier dataSource,DataSourceHolder<?> holder){
        if(dataSources.containsKey(dataSource)) return false;
        dataSources.put(dataSource,holder);
        holder.onAdded(this);
        markDataSourceDirty(dataSource);
        return true;
    }
    public boolean addDataSource(Identifier dataSource){
        if(!RPGEngineRegistries.DATA_SOURCE_REGISTRY.containsKey(dataSource)) return false;
        return addDataSource(dataSource,RPGEngineRegistries.DATA_SOURCE_REGISTRY.getValue(dataSource).createHolder(getRegistryAccess()));
    }
    public boolean addDataSource(DataSource<?> dataSource){
        return addDataSource(RPGEngineRegistries.DATA_SOURCE_REGISTRY.getKey(dataSource),dataSource.createHolder(getRegistryAccess()));
    }


    public boolean hasDataSource(Identifier dataSource){
        return dataSources.containsKey(dataSource);
    }
    public boolean hasDataSource(DataSource<?> dataSource){
        return dataSources.containsKey(RPGEngineRegistries.DATA_SOURCE_REGISTRY.getKey(dataSource));
    }

    public DataSourceHolder<?> getDataSourceHolder(DataSource<?> dataSource){
        return dataSources.get(RPGEngineRegistries.DATA_SOURCE_REGISTRY.getKey(dataSource));
    }
    public DataSourceHolder<?> getDataSourceHolder(Identifier dataSource){
        return dataSources.get(dataSource);
    }

    public <T extends DataSourceInstance<? extends DataSource<T>>> T getDataSource(Identifier dataSource,Class<T> clazz){
        DataSourceHolder<?> holder = getDataSourceHolder(dataSource);
        return holder != null && clazz.isInstance(holder.getDataSourceInstance()) ? clazz.cast(holder.getDataSourceInstance()) : null;
    }
    public <T extends DataSourceInstance<? extends DataSource<T>>> T getDataSource(DataSource<T> dataSource){
        DataSourceHolder<?> holder = getDataSourceHolder(dataSource);
        return holder != null && dataSource.getInstanceClass().isInstance(holder.getDataSourceInstance()) ? dataSource.getInstanceClass().cast(holder.getDataSourceInstance()) : null;
    }
    public DataSourceHolder<?> removeDataSourceHolder(Identifier dataSource){
        if(!dataSources.containsKey(dataSource)) return null;
        dataSources.get(dataSource).onRemoved(this);
        removedDataSources.add(dataSource);
        return dataSources.remove(dataSource);
    }

    //──Stat Sheet────────────────────────────────────────────────────────
    //TODO add methods for adding stats and multipliers


    public void addFlatStatModifier(Stat stat, Modifier<Double> modifier){
        statSheet.getStatInstance(stat).addFlatModifier(modifier);
        dirtyStats.add(stat);
    }
    public void addMultiplierStatModifier(Stat stat, Modifier<Double> modifier){
        statSheet.getStatInstance(stat).addMultiplierModifier(modifier);
        dirtyStats.add(stat);
    }


    public void removeStatModifier(Stat stat, Identifier modifier){
        statSheet.getStatInstance(stat).removeModifier(modifier);
        dirtyStats.add(stat);
    }


    public void updateEntityStatHolder(){
        if(dirtyStats.isEmpty()) return;
        for(LivingEntity entity : getAttachedEntities()){
            AscensionEntityDataProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(provider == null) continue;
            if(provider.getData() == null) continue;
            entity.getData(ZenithAttachments.STAT_HOLDER).updateStats(dirtyStats);
        }
        dirtyStats.clear();
    }

    @Override
    public Collection<Stat> getStats() {
        return statSheet.getAllStats();
    }

    @Override
    public ValueContainer<Double> getStatInstance(Stat stat) {
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

    private DataSourceHolder<?> loadDataSource(ValueInput input,RegistryAccess access){
        DataSourceHolder<?> holder = DataSourceHolder.load(input,access);
        if(holder == null){
            AscensionCraft.LOGGER.debug("ERROR LOADING DATA SOURCE");
        }
        return holder;

    }

    private void writeDataSource(Identifier dataSource, DataSourceHolder<?> holder, ValueOutput output, RegistryAccess access){
        if(holder == null) return;
        holder.write(output,access);
    }

    public OriginSourcePatch load(){
        if(cachedData == null) {
            finishLoading();
            return null;
        };
        loadOriginSourceData(cachedData);
        cachedData = null;
        return resolveFullPatch();
    }
    public void finishLoading(){
        NeoForge.EVENT_BUS.post(new OriginSourceEvent.OriginSourceFinishedLoadingEvent(this));
    }

    public void loadOriginSourceData(ValueInput input){
        HashMap<LoadPriority,ArrayList<DataSourceHolder<?>>> loadMap = new HashMap<>();
        ValueInput.ValueInputList inputList = input.childrenListOrEmpty("data_sources");
        for(ValueInput dataSourceInput : inputList){
            DataSourceHolder<?> holder = loadDataSource(dataSourceInput,getRegistryAccess());
            if(holder == null) continue;

            dataSources.put(holder.getDataSourceKey(),holder);
            if(holder.loadPriority() == LoadPriority.NO_LOAD) continue;

            loadMap.computeIfAbsent(holder.loadPriority(),key->new ArrayList<>()).add(holder);

        }

        for(LoadPriority priority : LoadPriority.values()){
            if(!loadMap.containsKey(priority)) continue;
            List<DataSourceHolder<?>> holders = loadMap.get(priority);

            for(DataSourceHolder<?> holder : holders){
                holder.onAdded(this);
            }
        }
        for(DataSourceHolder<?> holder : dataSources.values()) holder.preFinishedLoading(this);
        finishLoading();
        for(DataSourceHolder<?> holder : dataSources.values()) holder.finishedLoading(this);
    }

    public void writeOriginSourceData(ValueOutput output){

        ValueOutput.ValueOutputList outputList = output.childrenList("data_sources");
        for(Identifier dataSource : dataSources.keySet()){
            writeDataSource(dataSource,dataSources.get(dataSource),outputList.addChild(),getRegistryAccess());
        }
    }



    public void applyPatch(ByteBuf buf){
        boolean fullPatch = buf.readBoolean();
        ByteBufHelpers.decodeArray(buf, byteBuf -> {
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            DataSourceHolder<?> holder = getDataSourceHolder(identifier);
            if (holder != null) holder.decode(buf,getRegistryAccess(),fullPatch);
            else holder= DataSourceHolder.decode(identifier,buf,getRegistryAccess(),fullPatch);
            return new Pair<>(identifier, holder);
        }).forEach(pair->dataSources.put(pair.getFirst(),pair.getSecond()));

        ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier).forEach(dataSources::remove);
        ByteBufHelpers.decodeArray(buf, (byteBuf)->ValueContainer.decode(
                id->ZenithStatHelper.statInstance(ZenithStatHelper.stat(id)),
                byteBuf,
                Codec.DOUBLE
        )).forEach(statSheet::setStat);
    }

}
