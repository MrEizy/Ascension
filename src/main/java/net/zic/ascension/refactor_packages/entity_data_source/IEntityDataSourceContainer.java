package net.zic.ascension.refactor_packages.entity_data_source;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IEntityDataSourceContainer {


    void encode(RegistryFriendlyByteBuf buf);
    void write(CompoundTag tag);
    ResourceLocation getInstanceIdentifier();
    IEntityDataSource getDataSource();
}
