package net.thejadeproject.ascension.refactor_packages.entity_data_source;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface IEntityDataSourceContainer {


    void encode(RegistryFriendlyByteBuf buf);
    void write(CompoundTag tag);
    ResourceLocation getInstanceIdentifier();
    IEntityDataSource getDataSource();
}
