package net.zic.ascension.api.rpg_engine.source.data_source.v2;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.zenithlib.network.ByteBufHelpers;

public class DataSourceHolder<T extends DataSourceInstance>{
    private final DataSource<T> dataSource;
    private T dataSourceInstance;

    public DataSourceHolder(DataSource<T> dataSource,T dataSourceInstance) {
        this.dataSource = dataSource;
    }


    public DataSource<T> getDataSource(){
        return dataSource;
    }
    public Identifier getDataSourceKey(){
        //TODO update after changing registry to use new value
        return Identifier.parse("none");
    }
    public static DataSource<?> getDataSource(Identifier key){
        return null;
        //TODO return RPGEngineRegistries.DATA_SOURCE_REGISTRY.containsKey(key) ? RPGEngineRegistries.DATA_SOURCE_REGISTRY.getValue(key) : null;
    }


    public LoadPriority loadPriority() {
        return dataSource.loadPriority();
    }

    /**
     * called when the data source is added to an origin source
     * @param source the origin source it is being added to
     */
    public void onAdded(OriginSource source){
        dataSource.onAdded(source,dataSourceInstance);
    }

    /**
     * Called when the data source is removed from a source
     * @param source the source it is removed from
     */
    public void onRemoved(OriginSource source){
        dataSource.onRemoved(source,dataSourceInstance);
    }

    //called before finished loading, can safely be used for any finalized operations on other data sources
    public void preFinishedLoading(OriginSource source){
        dataSource.preFinishedLoading(source,dataSourceInstance);
    }
    /**
     * mainly used for cache clearing
     * Called when all data sources are finished being read
     * @param source the source it is loaded on
     */
    public void finishedLoading(OriginSource source){
        dataSource.finishedLoading(source,dataSourceInstance);
    }

    //called when the data source is added to the origin source, or a new entity holds the origin
    public void applyToEntity(LivingEntity entity){
        dataSource.applyToEntity(entity,dataSourceInstance);
    }

    //called when either an entity is detached from an origin or the data source is removed from the origin
    public void removeFromEntity(LivingEntity entity){
        dataSource.removeFromEntity(entity,dataSourceInstance);
    }

    public void writeInstance(ValueOutput output, RegistryAccess access){
        output.putString("data_source",getDataSourceKey().toString());
        dataSource.serializerHandler().write(dataSourceInstance,output.child("instance"),access);
    };

    public void encodeInstance(ByteBuf buf, RegistryAccess access, boolean fullPatch){
        ByteBufHelpers.encodeIdentifier(getDataSourceKey(),buf);
        dataSource.syncHandler(fullPatch).encode(dataSourceInstance,buf,access);
    }


    public void decode(ByteBuf buf,RegistryAccess access,boolean fullPatch){
        Identifier id = ByteBufHelpers.decodeIdentifier(buf);
        dataSource.syncHandler(fullPatch).decode(dataSourceInstance,buf,access);
    }
    public static DataSourceHolder<?> decode(ByteBuf buf,RegistryAccess access){
        DataSource<?> dataSource = getDataSource(ByteBufHelpers.decodeIdentifier(buf));
        if(dataSource == null) return null;

        return dataSource.createFormationInstance(buf,access);
    }
    public static DataSourceHolder<?> load(ValueInput input,RegistryAccess access){
        DataSource<?> dataSource = getDataSource(Identifier.parse(input.getStringOr("data_source","none")));
        if(dataSource ==null) return null;
        return dataSource.createFormationInstance(input,access);
    }
}
