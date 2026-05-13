package net.zic.ascension.refactor_packages.network.client_bound.entity_data.physique;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.forms.IEntityFormData;
import net.zic.ascension.refactor_packages.physiques.IPhysiqueData;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;
import net.zic.ascension.refactor_packages.util.ByteBufUtil;

public record SyncPhysique(ResourceLocation form, ResourceLocation physique, IPhysiqueData physiqueData) implements CustomPacketPayload {

    public static final Type<SyncPhysique> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_physique"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPhysique> STREAM_CODEC =
            StreamCodec.of(SyncPhysique::encode, SyncPhysique::decode);

    public static void encode(RegistryFriendlyByteBuf buf,SyncPhysique packet){
        ByteBufUtil.encodeString(buf,packet.form.toString());
        buf.writeBoolean(packet.physique != null);
        if(packet.physique != null) ByteBufUtil.encodeString(buf,packet.physique.toString());
        if(packet.physiqueData != null) packet.physiqueData.encode(buf);
    }
    public static SyncPhysique decode(RegistryFriendlyByteBuf buf){
        ResourceLocation form = ByteBufUtil.readResourceLocation(buf);
        ResourceLocation physique = null;
        IPhysiqueData physiqueData = null;
        if(buf.readBoolean()){
            physique = ByteBufUtil.readResourceLocation(buf);
            physiqueData = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physique).fromNetwork(buf);
        }
        return new SyncPhysique(form,physique,physiqueData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncPhysique payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            IEntityFormData formData = entityData.getEntityFormData(payload.form);
            formData.setPhysique(payload.physique,payload.physiqueData);

        });
    }
}
