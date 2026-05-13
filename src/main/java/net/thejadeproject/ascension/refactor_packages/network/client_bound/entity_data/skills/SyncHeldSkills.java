package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills;

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
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;

public record SyncHeldSkills(String form,HeldSkills heldSkills)implements CustomPacketPayload {

    public static final Type<SyncHeldSkills> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_held_skills"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHeldSkills> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncHeldSkills::form,
            StreamCodec.of(HeldSkills::encodeFull,HeldSkills::decodeFull),
            SyncHeldSkills::heldSkills,
            SyncHeldSkills::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncHeldSkills payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            IEntityFormData formData = entityData.getEntityFormData(ResourceLocation.parse(payload.form));
            formData.setHeldSkills(payload.heldSkills());
            //System.out.println("skill list synced to client");
        });
    }
}
