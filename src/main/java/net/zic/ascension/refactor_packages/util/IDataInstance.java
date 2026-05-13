package net.zic.ascension.refactor_packages.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IDataInstance {

    CompoundTag write();
    void encode(RegistryFriendlyByteBuf buf);
}
