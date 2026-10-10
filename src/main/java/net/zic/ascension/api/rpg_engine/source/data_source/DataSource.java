package net.zic.ascension.api.rpg_engine.source.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
/*
    TODO:
        I may be able to simplify this process even further, instead of requiring a data source the DataSource instance IS the data source.
        then when registering the data source they do so using a constructor, passing data like load prio and anynomus classes for methods onAdded etc
        There would still be an underlying registry but instead of creating a data source that provides instances, they define a type that provides a data source
        (AKA LIKE FULLY WHAT ATTACHMENTS ARE DOING IM JUST ERMAKING ECS HAHAHAHAHAHAHAHAHA i swear im not crazy)
        the main benifit is it further abstracts some of the details away from the user.
        I dont even need to change anything right now could just make a SimpleDataSource class that comes with a constructor
 */
public interface DataSource<T extends DataSourceInstance<? extends DataSource<T>>>{



    LoadPriority loadPriority();

    /**
     * called when the data source is added to an origin source
     * be aware that in most scenarios your instance will be empty when added in a non-loading context,
     * so you can use this knowledge to properly leverage methods like preFinishedLoading if you need to conditionally access
     * other Data sources (this is only if you defined created holders to be empty)
     * @param source the origin source it is being added to
     * @param instance the instance for this data source
     */
    void onAdded(OriginSource source, T instance);

    /**
     * Called when the data source is removed from a source
     * @param source the source it is removed from
     * @param instance the instance of this data source
     */
    void onRemoved(OriginSource source, T instance);

    //called before finished loading, can safely be used for any finalized operations on other data sources
    void preFinishedLoading(OriginSource source,T instance);
    /**
     * mainly used for cache clearing
     * Called when all data sources are finished being read
     * @param source the source it is loaded on
     * @param instance the instance of this data source
     */
    void finishedLoading(OriginSource source, T instance);

    //called when the data source is added to the origin source, or a new entity holds the origin
    void applyToEntity(LivingEntity entity, T instance);

    //called when either an entity is detached from an origin or the data source is removed from the origin
    void removeFromEntity(LivingEntity entity, T instance);

    T newInstance(RegistryAccess access);
    Class<T> getInstanceClass();
    SerializerHandler<T> serializerHandler();
    /**
     * returns a sync handler, allowing for different handlers depending on the type of syncing
     * NOTE full patch is different than an individual source handling full/partial. if full patch is true, it just means
     * the origin source needs all sources to fully sync (this is not needed for initial syncs,
     */
    SyncHandler<T> syncHandler(boolean fullPatch);

    default DataSourceHolder<T> createHolder(RegistryAccess access){
        return new DataSourceHolder<>(this,newInstance(access));
    }
    default DataSourceHolder<T> createHolder(ByteBuf buf, RegistryAccess access,boolean fullPatch){
        T instance = newInstance(access);
        syncHandler(fullPatch).decode(instance,buf,access);
        return new DataSourceHolder<>(this,instance);
    }
    default DataSourceHolder<T> createHolder(ValueInput input, RegistryAccess access){
        return new DataSourceHolder<>(this, serializerHandler().read(input,access));
    }
}
