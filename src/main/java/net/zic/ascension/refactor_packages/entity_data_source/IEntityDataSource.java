package net.zic.ascension.refactor_packages.entity_data_source;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;

public interface IEntityDataSource {

    void onAdded(IEntityData entityData,IEntityDataSourceContainer container);
    void onRemoved(IEntityData entityData,IEntityDataSourceContainer container);


    public void tick(IEntityData entityData,IEntityDataSourceContainer container);
    IEntityDataSourceContainer fromCompound(CompoundTag tag);
    IEntityDataSourceContainer fromNetwork(RegistryFriendlyByteBuf buf);
}
