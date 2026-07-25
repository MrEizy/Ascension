package net.zic.ascension.api.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;

public class SkillHolderProvider implements DataSource {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NORMAL;
    }
    protected SkillHolder getHolder(DataSourceInstance instance){
        return (SkillHolder) instance;
        //we want to throw an error for now
    }
    @Override
    public void onAdded(OriginSource source, DataSourceInstance instance) {
        SkillHolder holder = getHolder(instance);
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,source.getRegistryAccess()).onAdded(source,holder.getSkillData(skill));
        }
    }

    @Override
    public void onRemoved(OriginSource source, DataSourceInstance instance) {
        SkillHolder holder = getHolder(instance);
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,source.getRegistryAccess()).onRemoved(source,holder.getSkillData(skill));
        }
    }

    @Override
    public void finishedLoading(OriginSource source, DataSourceInstance instance) {
        getHolder(instance).clearCache();
    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourceInstance instance) {
        SkillHolder holder = getHolder(instance);
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,entity.level().registryAccess()).applyToEntity(entity,holder.getSkillData(skill));
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourceInstance instance) {
        SkillHolder holder = getHolder(instance);
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,entity.level().registryAccess()).removeFromEntity(entity,holder.getSkillData(skill));
        }
    }

    @Override
    public DataSourceInstance newInstance(RegistryAccess access) {
        return new SkillHolder();
    }

    @Override
    public DataSourceInstance loadInstance(ValueInput input, RegistryAccess access) {
        SkillHolder holder = new SkillHolder();
        holder.read(input,access);
        return holder;
    }

    @Override
    public DataSourceInstance loadInstance(ByteBuf buf, RegistryAccess access) {
        SkillHolder holder = new SkillHolder();
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
