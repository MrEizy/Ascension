package net.thejadeproject.ascension.refactor_packages.entity_data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;

public class EntityDataProvider implements IAttachmentSerializer<CompoundTag,IEntityData> {
    @Override
    public IEntityData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {

        if(holder instanceof Entity entity){
            //System.out.println("trying to create entity data");
            if(EntityDataManager.isWatching(entity.getUUID())) return new RemoteEntityData(entity,tag);
            return new GenericEntityData(entity,tag);
        }

        return null;
    }

    @Override
    public @Nullable CompoundTag write(IEntityData entityData, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        entityData.write(tag);
        //System.out.println("saving entity data");
        return tag;
    }
}
