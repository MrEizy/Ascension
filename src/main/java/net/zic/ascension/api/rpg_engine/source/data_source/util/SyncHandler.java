package net.zic.ascension.api.rpg_engine.source.data_source.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.StreamCodec;

//TODO move over to zenith lib
public interface SyncHandler<T>{
    void decode(T existingEncodable, ByteBuf buf, RegistryAccess access);
    void encode(T encodable, ByteBuf buf, RegistryAccess access);


    record StreamCodecSyncHandler<T>(StreamCodec<ByteBuf,T> codec) implements SyncHandler<T>{


        @Override
        public void decode(T existingEncodable, ByteBuf buf, RegistryAccess access) {
            //Does nothing by default
        }

        @Override
        public void encode(T encodable, ByteBuf buf, RegistryAccess access) {
            codec.encode(buf,encodable);
        }
    }
    //does nothing. used for things that do not sync
    final class UnitSyncHandler<T> implements SyncHandler<T>{


        @Override
        public void decode(T existingEncodable, ByteBuf buf, RegistryAccess access) {

        }

        @Override
        public void encode(T encodable, ByteBuf buf, RegistryAccess access) {

        }
    }
}
