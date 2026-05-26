package net.zic.ascension.api.core.bloodline;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.storage.ValueOutput;

public interface BloodlineData {
    int getPurity();


    void write(ValueOutput output);
    void encode(RegistryFriendlyByteBuf buf);
}
