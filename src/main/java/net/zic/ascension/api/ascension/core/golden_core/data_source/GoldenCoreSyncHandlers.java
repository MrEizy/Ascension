package net.zic.ascension.api.ascension.core.golden_core.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class GoldenCoreSyncHandlers {
    //syncs every core
    public static class FullPatchSyncHandler implements SyncHandler<GoldenCoreHolder> {
        @Override
        public void decode(GoldenCoreHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodeFull(buf, access);
        }

        @Override
        public void encode(GoldenCoreHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodeFull(buf, access);
        }
    }

    //syncs only the dirty and removed cores
    public static class PartialPatchSyncHandler implements SyncHandler<GoldenCoreHolder> {
        @Override
        public void decode(GoldenCoreHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodePartial(buf, access);
        }

        @Override
        public void encode(GoldenCoreHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodePartial(buf, access);
        }
    }
}
