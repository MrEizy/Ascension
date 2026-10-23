package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class TechniqueSyncHandlers {
    //syncs every technique
    public static class FullPatchSyncHandler implements SyncHandler<TechniqueHolder> {
        @Override
        public void decode(TechniqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodeFull(buf, access);
        }

        @Override
        public void encode(TechniqueHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodeFull(buf, access);
        }
    }

    //syncs only the dirty and removed techniques
    public static class PartialPatchSyncHandler implements SyncHandler<TechniqueHolder> {
        @Override
        public void decode(TechniqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodePartial(buf, access);
        }

        @Override
        public void encode(TechniqueHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodePartial(buf, access);
        }
    }
}
