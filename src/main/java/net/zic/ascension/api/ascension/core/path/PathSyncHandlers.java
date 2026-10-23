package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class PathSyncHandlers {
    //syncs every path
    public static class FullPatchSyncHandler implements SyncHandler<PathHolder> {
        @Override
        public void decode(PathHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodeFull(buf, access);
        }

        @Override
        public void encode(PathHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodeFull(buf, access);
        }
    }

    //syncs only the dirty and removed paths
    public static class PartialPatchSyncHandler implements SyncHandler<PathHolder> {
        @Override
        public void decode(PathHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodePartial(buf, access);
        }

        @Override
        public void encode(PathHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodePartial(buf, access);
        }
    }
}
