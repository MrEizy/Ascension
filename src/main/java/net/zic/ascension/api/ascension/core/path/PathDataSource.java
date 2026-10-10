package net.zic.ascension.api.ascension.core.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

import java.util.HashSet;
import java.util.Map;

public class PathDataSource implements DataSource<PathHolder> {
    public static final SerializerHandler<PathHolder> SERIALIZER_HANDLER = new PathSerializerHandler();
    public static final SyncHandler<PathHolder> FULL_PATCH_SYNC_HANDLER = new PathSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<PathHolder> PARTIAL_PATCH_SYNC_HANDLER = new PathSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NO_LOAD;
    }

    @Override
    public void onAdded(OriginSource source, PathHolder holder) {
        Map<Identifier, PathInstance> rawPaths = holder.getRawPathInstance();
        Map<Identifier, HashSet<Identifier>> rawOwners = holder.getRawPathOwnerData();

        holder.clearContainer();
        for(Identifier path : rawPaths.keySet()){
            for(Identifier owner : rawOwners.get(path)){
                AscensionOriginSourceHelper.addPath(source,path,rawPaths.get(path),owner);
            }
        }
    }

    @Override
    public void onRemoved(OriginSource source, PathHolder holder) {
         Map<Identifier,PathInstance> rawPaths = holder.getRawPathInstance();
        Map<Identifier, HashSet<Identifier>> rawOwners = holder.getRawPathOwnerData();

        for(Identifier path : rawPaths.keySet()){
            for(Identifier owner : rawOwners.get(path)){
                AscensionOriginSourceHelper.removePath(source,path,owner);
            }
        }
        holder.setRawData(rawPaths,rawOwners);
    }

    @Override
    public void preFinishedLoading(OriginSource source, PathHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, PathHolder holder) {
        for(Identifier path : holder.getPaths()){
            holder.getPath(path).simulateProgression(source);
        }
        holder.clearCache();
    }

    @Override
    public void applyToEntity(LivingEntity entity, PathHolder holder) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, PathHolder holder) {

    }

    @Override
    public PathHolder newInstance(RegistryAccess access) {
        return new PathHolder();
    }

    @Override
    public Class<PathHolder> getInstanceClass() {
        return PathHolder.class;
    }

    @Override
    public SerializerHandler<PathHolder> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<PathHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
