package net.zic.ascension.api.ascension.core.skill.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

import java.util.HashSet;
import java.util.Map;

public class SkillDataSource implements DataSource<SkillHolder> {
    public static final SerializerHandler<SkillHolder> SERIALIZER_HANDLER = new SkillSerializerHandler();
    public static final SyncHandler<SkillHolder> FULL_PATCH_SYNC_HANDLER = new SkillSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<SkillHolder> PARTIAL_PATCH_SYNC_HANDLER = new SkillSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NO_LOAD;
    }

    @Override
    public void onAdded(OriginSource source, SkillHolder holder) {
        Map<Identifier, SkillData> rawSkillData = holder.getRawSkillData();
        Map<Identifier, HashSet<Identifier>> rawOwners = holder.getRawSkillOwnerData();

        holder.clearContainer();
        for(Identifier path : rawSkillData.keySet()){
            for(Identifier owner : rawOwners.get(path)){
                AscensionOriginSourceHelper.addSkill(source,path,rawSkillData.get(path),owner);
            }
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillHolder holder) {
        Map<Identifier, SkillData> rawSkillData = holder.getRawSkillData();
        Map<Identifier, HashSet<Identifier>> rawOwners = holder.getRawSkillOwnerData();

        holder.clearContainer();
        for(Identifier path : rawSkillData.keySet()){
            for(Identifier owner : rawOwners.get(path)){
                AscensionOriginSourceHelper.removeSkill(source,path,owner);
            }
        }
        holder.setRawData(rawSkillData,rawOwners);
    }

    @Override
    public void preFinishedLoading(OriginSource source, SkillHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, SkillHolder holder) {
        holder.clearCache();
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillHolder holder) {
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,entity.level().registryAccess()).applyToEntity(entity,holder.getSkillData(skill));
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillHolder holder) {
        for(Identifier skill : holder.getSkills()){
            holder.getSkill(skill,entity.level().registryAccess()).removeFromEntity(entity,holder.getSkillData(skill));
        }
    }

    @Override
    public SkillHolder newInstance(RegistryAccess access) {
        return new SkillHolder();
    }

    @Override
    public Class<SkillHolder> getInstanceClass() {
        return SkillHolder.class;
    }

    @Override
    public SerializerHandler<SkillHolder> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<SkillHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
