package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class PathBonusHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }
    protected DataSourcePathBonusHolder getHolder(DataSourceInstance instance){
        return (DataSourcePathBonusHolder) instance;
        //we want to throw an error for now
    }
    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {

    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new DataSourcePathBonusHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        return newInstance(access);
    }

    @Override
    public DataSourceInstance loadInstance(DataSourceInstance previous,ByteBuf buf, RegistryAccess access) {
        DataSourcePathBonusHolder holder;
        if(previous==null) holder = new DataSourcePathBonusHolder();
        else holder = getHolder(previous);
        holder.decode(buf);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {

    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access,boolean fullPatch) {
        getHolder(instance).encode(buf,fullPatch);
    }
}
