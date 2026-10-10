package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.zenithlib.network.ByteBufHelpers;

public abstract class BloodlineSyncHandlers {

    public static class FullPatchSyncHandler implements SyncHandler<BloodlineHolder>{

        @Override
        public void decode(BloodlineHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.bloodlines.clear();
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier bloodlineId = ByteBufHelpers.decodeIdentifier(buf);
                Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodlineId,access);
                BloodlineData data = bloodline.loadData(buf);
                existingEncodable.bloodlines.put(bloodlineId,data);
            }
        }

        @Override
        public void encode(BloodlineHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.bloodlines.size());
            for(Identifier bloodline : encodable.bloodlines.keySet()){
                ByteBufHelpers.encodeIdentifier(bloodline,buf);
                encodable.getBloodline(bloodline).encode(buf,access);
            }
            encodable.dirtyBloodlines.clear();
            encodable. toRemoveBloodlines.clear();
        }
    }
    public static class PartialPatchSyncHandler implements SyncHandler<BloodlineHolder>{

        @Override
        public void decode(BloodlineHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier bloodlineId = ByteBufHelpers.decodeIdentifier(buf);
                Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodlineId,access);
                BloodlineData data = bloodline.loadData(buf);
                existingEncodable.bloodlines.put(bloodlineId,data);
            }
            ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(existingEncodable::removeBloodline);

        }

        @Override
        public void encode(BloodlineHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.dirtyBloodlines.size());
            for(Identifier dirtyBloodline : encodable.dirtyBloodlines){
                ByteBufHelpers.encodeIdentifier(dirtyBloodline,buf);
                encodable.getBloodline(dirtyBloodline).encode(buf,access);
            }
            ByteBufHelpers.encodeCollection(encodable.toRemoveBloodlines,buf,ByteBufHelpers::encodeIdentifier);
            encodable.dirtyBloodlines.clear();
            encodable. toRemoveBloodlines.clear();
        }
    }

}
