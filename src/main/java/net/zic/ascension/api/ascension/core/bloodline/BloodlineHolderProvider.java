package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolder;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class BloodlineHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }
    protected BloodlineHolder getHolder(DataSourceInstance instance){
        return (BloodlineHolder) instance;
        //we want to throw an error for now
    }
    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        BloodlineHolder holder = getHolder(instance);
        for(Identifier bloodline : holder.getBloodlines()){
            holder.getBloodline(bloodline,source.getRegistryAccess()).onAdded(source, holder.getBloodline(bloodline));
        }
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        BloodlineHolder holder = getHolder(instance);
        for(Identifier bloodline : holder.getBloodlines()){
            holder.getBloodline(bloodline,source.getRegistryAccess()).onRemoved(source, holder.getBloodline(bloodline));
        }
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {
        BloodlineHolder holder = getHolder(instance);
        for(Identifier bloodline : holder.getBloodlines()){
            holder.getBloodline(bloodline,entity.level().registryAccess()).applyToEntity(entity, holder.getBloodline(bloodline));
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {
        BloodlineHolder holder = getHolder(instance);
        for(Identifier bloodline : holder.getBloodlines()){
            holder.getBloodline(bloodline,entity.level().registryAccess()).removeFromEntity(entity, holder.getBloodline(bloodline));
        }
    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new BloodlineHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        BloodlineHolder holder =  new BloodlineHolder();
        holder.read(input,access);
        return holder;
    }

    @Override
    public DataSourceInstance loadInstance(DataSourceInstance previous,ByteBuf buf, RegistryAccess access) {
        BloodlineHolder holder;
        if(previous==null) holder = new BloodlineHolder();
        else holder = getHolder(previous);
        holder.decode(buf,access);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {
        BloodlineHolder holder = getHolder(instance);
        holder.write(output,access);
    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access,boolean fullPatch) {
        BloodlineHolder holder = getHolder(instance);
        holder.encode(buf,access,fullPatch);
    }
}
