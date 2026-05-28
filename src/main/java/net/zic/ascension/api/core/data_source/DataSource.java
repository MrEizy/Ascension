package net.zic.ascension.api.core.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.data_source.DataSourceType;

public interface DataSource {


    DataSourceType getType();
    /**
     * called when the data source is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this data source
     */
    void onAdded(OriginSource source, DataSourceInstance data);

    /**
     * Called when the data source is removed from a source
     * @param source the source it is removed from
     * @param data the data of this data source
     */
    void onRemoved(OriginSource source, DataSourceInstance data);

    //called when an entity that owns an origin detects the data source was changed
    void applyToEntity(LivingEntity entity, DataSourceInstance data);

    //called when either an entity is detached from an origin or the data source is removed from the origin
    void removeFromEntity(LivingEntity entity,DataSourceInstance data);


    DataSourceInstance newInstance();
    DataSourceInstance loadInstance(ValueInput input);
    DataSourceInstance loadInstance(ByteBuf buf);
}
