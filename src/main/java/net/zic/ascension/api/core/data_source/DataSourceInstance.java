package net.zic.ascension.api.core.data_source;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface DataSourceInstance {

    void write(ValueOutput output);
    void encode(RegistryFriendlyByteBuf buf);
}
