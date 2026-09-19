package net.zic.ascension.api.rpg_engine.source.data_source.v2.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.StreamCodec;

//TODO move over to zenith lib
public interface SyncHandler<T>{
    T decode(ByteBuf buf, RegistryAccess access);
    void decode(T existingEncodable,ByteBuf buf,RegistryAccess access);
    void encode(T encodable, ByteBuf buf, RegistryAccess access);


    record StreamCodecSyncHandler<T>(StreamCodec<ByteBuf,T> codec) implements SyncHandler<T>{

        @Override
        public T decode(ByteBuf buf, RegistryAccess access) {return codec.decode(buf);}

        @Override
        public void decode(T existingEncodable, ByteBuf buf, RegistryAccess access) {
            //Does nothing by default
        }

        @Override
        public void encode(T encodable, ByteBuf buf, RegistryAccess access) {
            codec.encode(buf,encodable);
        }
    }
}
