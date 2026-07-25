package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class PhysiqueHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        //TODO
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        //TODO
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {
        //TODO
    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance data) {
        //TODO
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance data) {
        //TODO
    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return null; //TODO
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        return null; //TODO
    }

    @Override
    public DataSourceInstance loadInstance(ByteBuf buf, RegistryAccess access) {
        return null; //TODO
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {
        //TODO
    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access) {
        //TODO
    }
}
