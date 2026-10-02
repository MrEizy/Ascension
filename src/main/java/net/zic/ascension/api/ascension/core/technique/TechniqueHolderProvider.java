package net.zic.ascension.api.ascension.core.technique;

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

import java.util.Map;

public class TechniqueHolderProvider implements DataSource {

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGH;
    }

    protected TechniqueHolder getHolder(DataSourceInstance instance) {
        return (TechniqueHolder) instance;
    }

    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        TechniqueHolder holder = getHolder(instance);
        Map<Identifier, TechniqueData> techniques = holder.getRawData();

        holder.clearContainer();
        for (Map.Entry<Identifier, TechniqueData> entry : techniques.entrySet()) {
            AscensionOriginSourceHelper.addTechnique(source, entry.getKey(), entry.getValue(), false);
        }
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        TechniqueHolder holder = getHolder(instance);
        Map<Identifier, TechniqueData> techniques = holder.getRawData();

        for (Identifier techniqueId : techniques.keySet()) {
            AscensionOriginSourceHelper.removeTechnique(source, techniqueId);
        }
        holder.setRawData(techniques);
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {
        TechniqueHolder holder = getHolder(instance);
        for (Identifier techniqueId : holder.getTechniques()) {
            Technique technique = holder.getTechnique(techniqueId, entity.level().registryAccess());
            if (technique != null) {
                technique.applyToEntity(entity, holder.getTechniqueData(techniqueId));
            }
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {
        TechniqueHolder holder = getHolder(instance);
        for (Identifier techniqueId : holder.getTechniques()) {
            Technique technique = holder.getTechnique(techniqueId, entity.level().registryAccess());
            if (technique != null) {
                technique.removeFromEntity(entity, holder.getTechniqueData(techniqueId));
            }
        }
    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new TechniqueHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        TechniqueHolder holder = new TechniqueHolder();
        holder.read(input, access);
        return holder;
    }

    @Override
    public DataSourceInstance loadInstance(DataSourceInstance previous, ByteBuf buffer, RegistryAccess access) {
        TechniqueHolder holder = previous == null ? new TechniqueHolder() : getHolder(previous);
        holder.decode(buffer, access);
        return holder;
    }

    @Override
    public void writeInstance(DataSourceInstance instance, ValueOutput output, RegistryAccess access) {
        getHolder(instance).write(output, access);
    }

    @Override
    public void encodeInstance(DataSourceInstance instance, ByteBuf buffer, RegistryAccess access, boolean fullPatch) {
        getHolder(instance).encode(buffer, access, fullPatch);
    }
}
