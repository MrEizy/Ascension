package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.path_data.SyncPathData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;

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