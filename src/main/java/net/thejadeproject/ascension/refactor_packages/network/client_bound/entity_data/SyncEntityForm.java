package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;

import java.nio.charset.Charset;

public record SyncEntityForm(IEntityFormData formData) implements CustomPacketPayload {

    public static final Type<SyncEntityForm> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_entity_form"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityForm> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(SyncEntityForm::encode,SyncEntityForm::decode),
            SyncEntityForm::formData,
            SyncEntityForm::new
    );
    public static void encode(RegistryFriendlyByteBuf buf,IEntityFormData formData){
        buf.writeInt(formData.getEntityFormId().toString().length());
        buf.writeCharSequence(formData.getEntityFormId().toString(), Charset.defaultCharset());
        //System.out.println("encoding form:"+formData.getEntityFormId().toString());
        formData.getEntityForm().encode(buf,formData);
    }
    public static IEntityFormData decode(RegistryFriendlyByteBuf buf){
        return AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(ResourceLocation.parse((String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset()))).fromNetwork(buf);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncEntityForm payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            entityData.setFormData(payload.formData.getEntityFormId(),payload.formData);
            //System.out.println("form : "+payload.formData.getEntityFormId().toString() +" was synced to the client");
            //TODO ensure path form locations are properly linked
        });
    }
}
