package net.zic.ascension.api.rpg_engine.source.data_source.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//TODO move over to zenith lib
public interface SerializerHandler<T>{

    T read(ValueInput input, RegistryAccess access);
    void write(T writable, ValueOutput output, RegistryAccess access);


    record CodecSerializerHandler<T>(Codec<T> codec) implements SerializerHandler<T>{

        @Override
        public T read(ValueInput input, RegistryAccess access) {return input.read("codec",codec).orElse(null);}

        @Override
        public void write(T writable, ValueOutput output, RegistryAccess access) {output.store("codec",codec,writable);}
    }
}