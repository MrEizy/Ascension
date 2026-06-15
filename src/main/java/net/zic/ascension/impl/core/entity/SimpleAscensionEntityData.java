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
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.ServerOriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SimpleAscensionEntityData implements AscensionEntityData {
    private final OriginSource source;
    private final LivingEntity attachedEntity;
    private SourceChangesSnapshot snapshot;
    private boolean cultivationSuppressed;

    public SimpleAscensionEntityData(OriginSource source, LivingEntity entity) {
        this.source = source;
        this.attachedEntity = entity;
        if (!attachedEntity.level().isClientSide()) {
            AscensionCraft.getSourceHandler().addWatcher(attachedEntity, source);
        }
    }

    public void initializeAttributes() {
        ZenithAttributeHolder attributeHolder = attachedEntity.getData(
                ZenithAttachments.ATTRIBUTE_HOLDER
        );

        attributeHolder.addAttribute(Attributes.MAX_HEALTH);
        attributeHolder.getAttribute(Attributes.MAX_HEALTH).addStatScaling(
                AscensionStats.VITALITY.get(),
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "base_scaling"),
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
        source.load();
        initializeAttributes();
        AscensionCraft.getSourceHandler().applyToWatcher(attachedEntity);

        if (!attachedEntity.level().isClientSide()) {
            snapshot = null;
            attachedEntity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        }
    }

    @Override
    public void markDirty(SourceChangesSnapshot snapshot) {
        if (attachedEntity.level().isClientSide()) {
            return;
        }

        this.snapshot = snapshot;
        attachedEntity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    @Override
    public boolean isCultivationSuppressed() {
        return cultivationSuppressed;
    }

    @Override
    public void setCultivationSuppressed(boolean state) {
        cultivationSuppressed = state;
    }

    public static class SyncHandler implements AttachmentSyncHandler<SimpleAscensionEntityData> {
        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(
                RegistryFriendlyByteBuf buf,
                SimpleAscensionEntityData attachment,
                boolean initialSync
        ) {
            buf.writeBoolean(attachment.cultivationSuppressed);
            boolean encodePatch = !initialSync && attachment.snapshot != null;
            buf.writeBoolean(encodePatch);

            if (encodePatch) {
                attachment.snapshot.encode(buf);
            } else {
                attachment.source.encode(buf);
            }
            attachment.snapshot = null;
        }

        @Override
        public @Nullable SimpleAscensionEntityData read(
                IAttachmentHolder holder,
                RegistryFriendlyByteBuf buf,
                @Nullable SimpleAscensionEntityData previousValue
        ) {
            if (!(holder instanceof LivingEntity entity)) {
                return null;
            }

            if (previousValue == null) {
                previousValue = new SimpleAscensionEntityData(
                        new OriginSource(buf.registryAccess()),
                        entity
                );
            }
            previousValue.setCultivationSuppressed(buf.readBoolean());
            if (buf.readBoolean()) {
                previousValue.source.apply(
                        SourceChangesSnapshot.decode(buf, buf.registryAccess())
                );
            } else {
                previousValue.source.decode(buf);
            }

            previousValue.source.updateAttributes(
                    entity.getData(ZenithAttachments.ATTRIBUTE_HOLDER)
            );
            return previousValue;
        }
    }

    public static class Provider implements IAttachmentSerializer<SimpleAscensionEntityData> {
        @Override
        public SimpleAscensionEntityData read(
                @NonNull IAttachmentHolder holder,
                ValueInput input
        ) {
            if (!(holder instanceof LivingEntity entity)) {
                return null;
            }

            OriginSource source = entity.level().isClientSide()
                    ? new OriginSource(entity.level().registryAccess(), input.childOrEmpty("source_data"))
                    : new ServerOriginSource(entity.level().registryAccess(), input.childOrEmpty("source_data"));

            SimpleAscensionEntityData data = new SimpleAscensionEntityData(source,entity);

            data.setCultivationSuppressed(input.getBooleanOr("cultivation_suppressed",false));

            return data;

        }

        @Override
        public boolean write(SimpleAscensionEntityData attachment, ValueOutput output) {
            attachment.source.write(output.child("source_data"));
            output.putBoolean("cultivation_suppressed",attachment.isCultivationSuppressed());
            return true;
        }
    }
}
