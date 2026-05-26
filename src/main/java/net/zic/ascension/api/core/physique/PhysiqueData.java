package net.zic.ascension.api.core.physique;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface PhysiqueData {





    void write(ValueOutput output);
    void encode(RegistryFriendlyByteBuf buf);
}
