package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
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


        if(holder.getPhysique() == null) return;
        Identifier physique = holder.getPhysique();
        PhysiqueData data = holder.getData();
        holder.setPhysique(null,null);
        AscensionOriginSourceHelper.setPhysique(source,physique,data,false);
        //TODO fix other holders to do this
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        //TODOD update to allow setPhysique to take in a null value
        PhysiqueHolder holder = getHolder(instance);

        if(holder.getPhysique() == null) return;
        Identifier oldPhysique = holder.getPhysique();
        PhysiqueData oldPhysiqueData = holder.getData();

        AscensionOriginSourceHelper.setPhysique(source,null);

        holder.setPhysique(oldPhysique,oldPhysiqueData);
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        if (holder.getPhysique() == null || holder.getData() == null) {
            return;
        }

        Physique physique = holder.getPhysique(entity.level().registryAccess());
        if (physique == null) {
            return;
        }

        physique.applyToEntity(entity, holder.getData());
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {
        PhysiqueHolder holder = getHolder(instance);

        if (holder.getPhysique() == null || holder.getData() == null) {
            return;
        }

        Physique physique = holder.getPhysique(entity.level().registryAccess());
        if (physique == null) {
            return;
        }

        physique.removeFromEntity(entity, holder.getData());

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
    public void encodeInstance(DataSourceInstance instance, ByteBuf buf, RegistryAccess access,boolean fullPatch) {
        PhysiqueHolder holder = getHolder(instance);
        holder.encode(buf,access);
    }
}
