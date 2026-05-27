package net.zic.ascension.core.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.ServerOriginSource;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.core.source.SourceHandler;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SimpleAscensionEntityData implements AscensionEntityData {

    private OriginSource source;

    private ValueInput cachedData;
    private final LivingEntity attachedEntity;

    public SimpleAscensionEntityData(OriginSource source,ValueInput input,LivingEntity entity) {
        this.source = source;
        cachedData = input;
        attachedEntity = entity;
    }

    public SimpleAscensionEntityData(OriginSource source,LivingEntity entity) {
        this.source = source;
        attachedEntity = entity;
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

        source.load(cachedData);
        cachedData = null;

        //TODO make sure this properly handles simulation of adding
        //TODO make sure the PathData knows which side it is on for event handling

        initializeAttributes();
        SourceHandler.addWatcher(attachedEntity,getSource());
    }

    public static class SyncHandler implements AttachmentSyncHandler<SimpleAscensionEntityData> {

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, SimpleAscensionEntityData attachment, boolean initialSync) {
            attachment.getSource().encode(buf);
        }

        @Override
        public @Nullable SimpleAscensionEntityData read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable SimpleAscensionEntityData previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new SimpleAscensionEntityData(new OriginSource(entity.level().registryAccess()),entity);
            previousValue.getSource().decode(buf);
            return previousValue;
        }
    }
    public static class Provider implements IAttachmentSerializer<SimpleAscensionEntityData> {

        @Override
        public SimpleAscensionEntityData read(@NonNull IAttachmentHolder holder, ValueInput input) {
            if(holder instanceof LivingEntity entity){
                OriginSource source =
                        entity.level().isClientSide() ?
                        new OriginSource(entity.level().registryAccess()) :
                        new ServerOriginSource(entity.level().registryAccess());
                return new SimpleAscensionEntityData(source,input,entity);
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
