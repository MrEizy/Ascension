package net.zic.ascension.api.ascension.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface RegistryObjectData {
    void write(ValueOutput output);
    void encode(ByteBuf buf);
}
