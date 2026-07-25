package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolder;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolder;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class PathHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NORMAL;
    }
    protected PathHolder getHolder(DataSourceInstance instance){
        return (PathHolder) instance;
        //we want to throw an error for now
    }
    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        PathHolder holder = getHolder(instance);
        for(Identifier path : holder.getPaths()){
            holder.getPath(path).simulateProgression(source);
        }
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        PathHolder holder = getHolder(instance);
        for(Identifier path : holder.getPaths()){
            holder.getPath(path).removeFromSource(source);
        }
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {
        getHolder(instance).clearCache();
    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {

    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new PathHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        PathHolder holder = new PathHolder();
        holder.read(input,access);
        return holder;
    }

    @Override
    public DataSourceInstance loadInstance(ByteBuf buf, RegistryAccess access) {
        PathHolder holder = new PathHolder();
        holder.decode(buf,access);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {
        getHolder(instance).write(output,access);
    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access) {
        getHolder(instance).encode(buf,access);
    }
}
