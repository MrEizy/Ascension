package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.path_data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public record RemovePathData(ResourceLocation form, ResourceLocation path) implements CustomPacketPayload {

    public static final Type<RemovePathData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "remove_path_data")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePathData> STREAM_CODEC =
            StreamCodec.of(RemovePathData::encode, RemovePathData::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, RemovePathData payload) {
        ByteBufUtil.encodeString(buf, payload.form().toString());
        ByteBufUtil.encodeString(buf, payload.path().toString());
    }

    private static RemovePathData decode(RegistryFriendlyByteBuf buf) {
        return new RemovePathData(ByteBufUtil.readResourceLocation(buf), ByteBufUtil.readResourceLocation(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(RemovePathData payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);

            IEntityFormData formData = entityData.getEntityFormData(payload.form());

            if (formData != null) {
                formData.removePathData(payload.path());
            }

            entityData.clearPathForm(payload.path());
        });
    }
}