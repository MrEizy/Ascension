package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolder;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class PathBonusHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }
    protected PathBonusHolder getHolder(DataSourceInstance instance){
        return (PathBonusHolder) instance;
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
        return new PathBonusHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        PathBonusHolder holder = new PathBonusHolder();
        return null;
    }

    @Override
    public DataSourceInstance loadInstance(DataSourceInstance previous,ByteBuf buf, RegistryAccess access) {
        PathBonusHolder holder;
        if(previous==null) holder = new PathBonusHolder();
        else holder = getHolder(previous);
        holder.decode(buf);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {

    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access) {

    }
}
