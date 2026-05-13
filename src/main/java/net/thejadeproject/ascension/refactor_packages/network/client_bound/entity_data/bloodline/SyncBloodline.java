package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.bloodline;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public record SyncBloodline(ResourceLocation form, ResourceLocation bloodline, IBloodlineData bloodlineData)
        implements CustomPacketPayload {

    public static final Type<SyncBloodline> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sync_bloodline"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBloodline> STREAM_CODEC =
            StreamCodec.of(SyncBloodline::encode, SyncBloodline::decode);

    public static void encode(RegistryFriendlyByteBuf buf, SyncBloodline packet) {
        ByteBufUtil.encodeString(buf, packet.form.toString());

        buf.writeBoolean(packet.bloodline != null);
        if (packet.bloodline != null) {
            ByteBufUtil.encodeString(buf, packet.bloodline.toString());
        }

        if (packet.bloodlineData != null) {
            packet.bloodlineData.encode(buf);
        }
    }

    public static SyncBloodline decode(RegistryFriendlyByteBuf buf) {
        ResourceLocation form = ByteBufUtil.readResourceLocation(buf);

        ResourceLocation bloodline = null;
        IBloodlineData bloodlineData = null;

        if (buf.readBoolean()) {
            bloodline = ByteBufUtil.readResourceLocation(buf);
            bloodlineData = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY
                    .get(bloodline)
                    .fromNetwork(buf);
        }

        return new SyncBloodline(form, bloodline, bloodlineData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(SyncBloodline payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            IEntityFormData formData = entityData.getEntityFormData(payload.form);

            if (formData == null) return;

            formData.setBloodline(payload.bloodline, payload.bloodlineData);


            entityData.setFormData(payload.form, formData);
        });
    }
}