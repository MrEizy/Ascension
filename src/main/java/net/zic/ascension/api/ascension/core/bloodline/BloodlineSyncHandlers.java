package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class BloodlineSyncHandlers {
    //syncs every bloodline
    public static class FullPatchSyncHandler implements SyncHandler<BloodlineHolder> {
        @Override
        public void decode(BloodlineHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodeFull(buf, access);
        }

        @Override
        public void encode(BloodlineHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodeFull(buf, access);
        }
    }

    //syncs only the dirty and removed bloodlines
    public static class PartialPatchSyncHandler implements SyncHandler<BloodlineHolder> {
        @Override
        public void decode(BloodlineHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodePartial(buf, access);
        }

        @Override
        public void encode(BloodlineHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodePartial(buf, access);
        }
    }
}
