package net.zic.ascension.api.core.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface DataSourceInstance {

    void write(ValueOutput output);
    void encode(ByteBuf buf);
}
