package net.zic.ascension.api.ascension.core.golden_core.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GoldenCoreDataSource implements DataSource<GoldenCoreHolder> {
    public static final SerializerHandler<GoldenCoreHolder> SERIALIZER_HANDLER = new GoldenCoreSerializerHandler();
    public static final SyncHandler<GoldenCoreHolder> FULL_PATCH_SYNC_HANDLER = new GoldenCoreSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<GoldenCoreHolder> PARTIAL_PATCH_SYNC_HANDLER = new GoldenCoreSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NO_LOAD;
    }

    @Override
    public void onAdded(OriginSource source, GoldenCoreHolder holder) {
        Map<UUID, GoldenCoreInstance<?>> rawCores = holder.getGoldenCoreSnapshot();
        Map<UUID, Set<Identifier>> rawOwners = holder.getOwnerSnapshot();

        holder.clearContainer();
        rawCores.forEach((id, instance) -> {
            for(Identifier owner : rawOwners.getOrDefault(id, Set.of())){
                AscensionOriginSourceHelper.addGoldenCore(source,instance,owner);
            }
        });
    }

    @Override
    public void onRemoved(OriginSource source, GoldenCoreHolder holder) {
        Map<UUID, GoldenCoreInstance<?>> rawCores = holder.getGoldenCoreSnapshot();
        Map<UUID, Set<Identifier>> rawOwners = holder.getOwnerSnapshot();

        rawCores.keySet().forEach(id -> {
            for(Identifier owner : rawOwners.getOrDefault(id, Set.of())){
                AscensionOriginSourceHelper.removeGoldenCore(source,id,owner);
            }
        });
        holder.restore(rawCores,rawOwners);
    }

    @Override
    public void preFinishedLoading(OriginSource source, GoldenCoreHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, GoldenCoreHolder holder) {
        holder.clearCache();
    }

    @Override
    public void applyToEntity(LivingEntity entity, GoldenCoreHolder holder) {
        for(GoldenCoreInstance<?> instance : holder.getGoldenCoreInstances()){
            instance.applyToEntity(entity);
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, GoldenCoreHolder holder) {
        for(GoldenCoreInstance<?> instance : holder.getGoldenCoreInstances()){
            instance.removeFromEntity(entity);
        }
    }

    @Override
    public GoldenCoreHolder newInstance(RegistryAccess access) {
        return new GoldenCoreHolder();
    }

    @Override
    public Class<GoldenCoreHolder> getInstanceClass() {
        return GoldenCoreHolder.class;
    }

    @Override
    public SerializerHandler<GoldenCoreHolder> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<GoldenCoreHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
