package net.zic.ascension.api.core.technique;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface TechniqueData {

    void write(ValueOutput output);
    void encode(RegistryFriendlyByteBuf buf);
}
