package net.zic.ascension.impl.core.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.ServerOriginSource;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SimpleAscensionEntityData implements AscensionEntityData {

    private final OriginSource source;

    private SourceChangesSnapshot snapshot;

    private final LivingEntity attachedEntity;

    public SimpleAscensionEntityData(OriginSource source,LivingEntity entity) {
        this.source = source;
        attachedEntity = entity;
        System.out.println("created data attachment on side :"+(entity.level().isClientSide()?"Client" :"Server"));
        System.out.println(source.getClass().getName());;
        if(!attachedEntity.level().isClientSide()) AscensionCraft.getSourceHandler().addWatcher(attachedEntity,source);
    }

    public void initializeAttributes(){
        ZenithAttributeHolder attributeHolder = attachedEntity.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

        attributeHolder.addAttribute(Attributes.MAX_HEALTH);
        attributeHolder.getAttribute(Attributes.MAX_HEALTH).addStatScaling(
                AscensionStats.VITALITY.get(), Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"base_scaling"),
                2
                );
        source.updateAttributes(attributeHolder);
    }

    @Override
    public LivingEntity getEntity() {
        return attachedEntity;
    }

    @Override
    public OriginSource getSource() {
        return source;
    }

    @Override
    public void initialize() {

        //TODO make sure this properly handles simulation of adding
        //TODO make sure the PathData knows which side it is on for event handling
        //TODO potentially have the source call the events (as in the path tells source hey call this event)
        //TODO and blank source does nothing
        source.load();


        initializeAttributes();

        AscensionCraft.getSourceHandler().applyToWatcher(attachedEntity);

    }

    @Override
    public void markDirty(SourceChangesSnapshot snapshot) {
        System.out.println("marked dirty starting synced on side : "+(attachedEntity.level().isClientSide()?"Client":"Server"));
        this.snapshot = snapshot;
        //TODO this is not working properly look into it
         attachedEntity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }


    public static class SyncHandler implements AttachmentSyncHandler<SimpleAscensionEntityData> {

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, SimpleAscensionEntityData attachment, boolean initialSync) {
            buf.writeBoolean(attachment.snapshot != null);//if true we are encoding a patch otherwise full encode
            System.out.println("trying to sync patch : "+(attachment.snapshot != null));
            if(attachment.snapshot == null) attachment.getSource().encode(buf);
            else attachment.snapshot.encode(buf);
        }

        @Override
        public @Nullable SimpleAscensionEntityData read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable SimpleAscensionEntityData previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new SimpleAscensionEntityData(new OriginSource(buf.registryAccess()),entity);
            if(buf.readBoolean()){
                AscensionCraft.LOGGER.debug("decoding patch");
                previousValue.getSource().apply(SourceChangesSnapshot.decode(buf,buf.registryAccess()));
            }else {
                AscensionCraft.LOGGER.debug("decoding full");
                previousValue.getSource().decode(buf);
            };
            previousValue.getSource().updateAttributes(entity.getData(ZenithAttachments.ATTRIBUTE_HOLDER));
            return previousValue;
        }
    }
    public static class Provider implements IAttachmentSerializer<SimpleAscensionEntityData> {

        @Override
        public SimpleAscensionEntityData read(@NonNull IAttachmentHolder holder, ValueInput input) {
            if(holder instanceof LivingEntity entity){
                OriginSource source =
                        entity.level().isClientSide() ?
                        new OriginSource(entity.level().registryAccess(),input) :
                        new ServerOriginSource(entity.level().registryAccess(),input);
                return new SimpleAscensionEntityData(source,entity);
            }
            return null;
        }

        @Override
        public boolean write(SimpleAscensionEntityData attachment, ValueOutput output) {
            attachment.getSource().write(output);
            return true;
        }
    }
}
