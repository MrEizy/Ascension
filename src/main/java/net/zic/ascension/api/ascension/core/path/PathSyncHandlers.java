package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.zenithlib.network.ByteBufHelpers;

public abstract class PathSyncHandlers {

    public static class FullPatchSyncHandler implements SyncHandler<PathHolder> {

        @Override
        public void decode(PathHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.paths.clear();
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier pathId = ByteBufHelpers.decodeIdentifier(buf);
                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
                PathInstance data = path.loadInstance(buf,access);
                existingEncodable.paths.put(pathId,data);
            }
            existingEncodable.dirtyPaths.clear();
            existingEncodable.toRemovePaths.clear();
        }

        @Override
        public void encode(PathHolder encodable, ByteBuf buf, RegistryAccess access) {

            buf.writeInt(encodable.paths.size());
            for(Identifier path : encodable.paths.keySet()){
                ByteBufHelpers.encodeIdentifier(path,buf);
                encodable.getPath(path).encode(buf,access);
            }
            encodable.dirtyPaths.clear();
            encodable.toRemovePaths.clear();
        }
    }
    public static class PartialPatchSyncHandler implements SyncHandler<PathHolder>{

        @Override
        public void decode(PathHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier pathId = ByteBufHelpers.decodeIdentifier(buf);
                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
                PathInstance data = path.loadInstance(buf,access);
                existingEncodable.paths.put(pathId,data);
            }
            ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(existingEncodable.paths::remove);

            existingEncodable.dirtyPaths.clear();
            existingEncodable.toRemovePaths.clear();
        }

        @Override
        public void encode(PathHolder encodable, ByteBuf buf, RegistryAccess access) {

            buf.writeInt(encodable.dirtyPaths.size());
            for(Identifier dirtyPath : encodable.dirtyPaths){
                ByteBufHelpers.encodeIdentifier(dirtyPath,buf);
                encodable.getPath(dirtyPath).encode(buf,access);
            }
            ByteBufHelpers.encodeCollection(encodable.toRemovePaths,buf,ByteBufHelpers::encodeIdentifier);
            encodable.dirtyPaths.clear();
            encodable.toRemovePaths.clear();
        }
    }

}
