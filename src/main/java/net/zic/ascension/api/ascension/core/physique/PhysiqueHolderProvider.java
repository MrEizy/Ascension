package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.event.EventReason;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class PhysiqueHolderProvider implements DataSource {

    //──Implementation────────────────────────────────────────────────────────
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    protected PhysiqueHolder getHolder(DataSourceInstance instance){
        return (PhysiqueHolder) instance;
        //we want to throw an error for now
    }

    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        //TODO update to use new source
        holder.getPhysique(source.getRegistryAccess()).onAdded(source,holder.getData());
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        //TODO update to use new source
        holder.getPhysique(source.getRegistryAccess()).onRemoved(source,holder.getData());
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        holder.getPhysique(entity.level().registryAccess()).applyToEntity(entity,holder.getData());
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        holder.getPhysique(entity.level().registryAccess()).removeFromEntity(entity,holder.getData());

    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new PhysiqueHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        PhysiqueHolder holder =  new PhysiqueHolder();
        holder.read(input,access);
        return holder;
    }

    @Override
    public DataSourceInstance loadInstance(DataSourceInstance previous,ByteBuf buf, RegistryAccess access) {
        PhysiqueHolder holder;
        if(previous==null) holder = new PhysiqueHolder();
        else holder = getHolder(previous);
        holder.decode(buf,access);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {
        PhysiqueHolder holder = getHolder(instance);
        holder.write(output,access);
    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access) {
        PhysiqueHolder holder = getHolder(instance);
        holder.encode(buf,access);
    }
}
