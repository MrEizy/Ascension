package net.zic.ascension.api.rpg_engine.source.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;


public interface DataSource {

    LoadPriority loadPriority();

    /**
     * called when the data source is added to an origin source
     * @param source the origin source it is being added to
     * @param instance the instance for this data source
     */
    void onAdded(OriginSource source, DataSourceInstance instance);

    /**
     * Called when the data source is removed from a source
     * @param source the source it is removed from
     * @param instance the instance of this data source
     */
    void onRemoved(OriginSource source, DataSourceInstance instance);

    /**
     * mainly used for cache clearing
     * Called when all data sources are finished being read
     * @param source the source it is loaded on
     * @param instance the instance of this data source
     */
    void finishedLoading(OriginSource source, DataSourceInstance instance);

    //called when the data source is added to the origin source, or a new entity holds the origin
    void applyToEntity(LivingEntity entity, DataSourceInstance instance);

    //called when either an entity is detached from an origin or the data source is removed from the origin
    void removeFromEntity(LivingEntity entity, DataSourceInstance instance);


    DataSourceInstance newInstance(RegistryAccess access);
    DataSourceInstance loadInstance(ValueInput input, RegistryAccess access);
    DataSourceInstance loadInstance(DataSourceInstance previousInstance,ByteBuf buf, RegistryAccess access);

    void writeInstance(DataSourceInstance instance,ValueOutput output, RegistryAccess access);

    void encodeInstance(DataSourceInstance instance,ByteBuf buf,RegistryAccess access,boolean fullPatch);

    static DataSource getInstance(Identifier identifier){
        return RPGEngineRegistries.DATA_SOURCE_REGISTRY.getValue(identifier);
    }
    static Identifier getId(DataSource source){
        return RPGEngineRegistries.DATA_SOURCE_REGISTRY.getKey(source);
    }
}
