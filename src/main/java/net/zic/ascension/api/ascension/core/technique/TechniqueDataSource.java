package net.zic.ascension.api.ascension.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

import java.util.Map;

public class TechniqueDataSource implements DataSource<TechniqueHolder> {
    public static final SerializerHandler<TechniqueHolder> SERIALIZER_HANDLER = new TechniqueSerializerHandler();
    public static final SyncHandler<TechniqueHolder> FULL_PATCH_SYNC_HANDLER = new TechniqueSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<TechniqueHolder> PARTIAL_PATCH_SYNC_HANDLER = new TechniqueSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGH;
    }

    @Override
    public void onAdded(OriginSource source, TechniqueHolder holder) {
        Map<Identifier, TechniqueInstance<?>> techniques = holder.getTechniqueSnapshot();

        holder.clearContainer();
        for (TechniqueInstance<?> instance : techniques.values()) {
            AscensionOriginSourceHelper.addTechnique(source, instance, false);
        }
    }

    @Override
    public void onRemoved(OriginSource source, TechniqueHolder holder) {
        Map<Identifier, TechniqueInstance<?>> techniques = holder.getTechniqueSnapshot();

        for (Identifier techniqueId : techniques.keySet()) {
            AscensionOriginSourceHelper.removeTechnique(source, techniqueId);
        }
        holder.restore(techniques);
    }

    @Override
    public void preFinishedLoading(OriginSource source, TechniqueHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, TechniqueHolder holder) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, TechniqueHolder holder) {
        for (TechniqueInstance<?> instance : holder.getTechniqueInstances()) {
            instance.applyToEntity(entity);
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, TechniqueHolder holder) {
        for (TechniqueInstance<?> instance : holder.getTechniqueInstances()) {
            instance.removeFromEntity(entity);
        }
    }

    @Override
    public TechniqueHolder newInstance(RegistryAccess access) {
        return new TechniqueHolder();
    }

    @Override
    public Class<TechniqueHolder> getInstanceClass() {
        return TechniqueHolder.class;
    }

    @Override
    public SerializerHandler<TechniqueHolder> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<TechniqueHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
