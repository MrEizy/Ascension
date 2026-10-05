package net.zic.ascension.api.ascension.core.path.bonus;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static net.zic.ascension.api.ascension.core.CoreAttachments.PATH_BONUS_HOLDER;

public class EntityPathBonusHolder extends MultiSourcePathBonusHolder{
    private final LivingEntity attachedEntity;

    public EntityPathBonusHolder(LivingEntity attachedEntity) {
        super(); //ensures the on resolved is set
        this.attachedEntity = attachedEntity;

    }

    @Override
    public void cachedHolderUpdated() {
        sync();
    }
    public void sync(){
        if(attachedEntity == null) return;
        attachedEntity.syncData(PATH_BONUS_HOLDER);//TODO replace with attachment
    }

    public static class SyncHandler implements AttachmentSyncHandler<EntityPathBonusHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, EntityPathBonusHolder attachment, boolean initialSync) {
            PathBonusHolder cachedHolder = attachment.getCachedPathBonusHolder();
            buf.writeInt(cachedHolder.dirtyBonuses().size());
            for(PathBonus bonus : cachedHolder.dirtyBonuses()){
                ByteBufHelpers.encodeIdentifier(bonus.category(),buf);
                ValueContainer.encode(cachedHolder.getContainer(bonus.category(),bonus.path()),buf,Codec.DOUBLE);
            }
            buf.writeInt(cachedHolder.removedBonuses().size());
            for(PathBonus bonus : cachedHolder.removedBonuses()){
                ByteBufHelpers.encodeIdentifier(bonus.category(),buf);
                ByteBufHelpers.encodeIdentifier(bonus.path(),buf);
            }
            cachedHolder.clearCache();
        }

        @Override
        public @Nullable EntityPathBonusHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable EntityPathBonusHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new EntityPathBonusHolder(entity);
            PathBonusHolder cachedHolder = previousValue.getCachedPathBonusHolder();
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier category = buf.readIdentifier();
                ValueContainer<Double> container = ValueContainer.normalDecode(ValueContainerHelpers::doubleValueContainer,buf,Codec.DOUBLE);
                cachedHolder.setPathBonusContainer(category,container);
            }
            size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier category = buf.readIdentifier();
                Identifier path = buf.readIdentifier();
                cachedHolder.removePathBonusContainer(category,path);
            }

            cachedHolder.clearCache();
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }
}
