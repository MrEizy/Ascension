package net.zic.ascension.refactor_packages.network.client_bound.entity_data.attributes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;

public record SyncAttributeHolder(AscensionAttributeHolder holder)implements CustomPacketPayload {

    public static final Type<SyncAttributeHolder> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_attribute_holder"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAttributeHolder> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(AscensionAttributeHolder::encode,AscensionAttributeHolder::decode),
            SyncAttributeHolder::holder,
            SyncAttributeHolder::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncAttributeHolder payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            entityData.setAscensionAttributeHolder(context.player(),payload.holder);
            entityData.getAscensionAttributeHolder().updateAttributes(entityData);
            //System.out.println("attributes synced");
            //System.out.println(context.player().getAttribute(Attributes.MAX_HEALTH).getValue());
            entityData.getAscensionAttributeHolder().log();
        });
    }
}