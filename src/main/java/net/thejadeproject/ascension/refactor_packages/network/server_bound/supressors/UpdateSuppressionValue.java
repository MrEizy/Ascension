package net.thejadeproject.ascension.refactor_packages.network.server_bound.supressors;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.techniques.MergeResponsePayload;
import net.thejadeproject.ascension.refactor_packages.techniques.merge.TechniqueMergeHandler;

public record UpdateSuppressionValue(String attribute, double val) implements CustomPacketPayload {

    public static final Type<UpdateSuppressionValue> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "update_suppression_value"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSuppressionValue> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    UpdateSuppressionValue::attribute,
                    ByteBufCodecs.DOUBLE,
                    UpdateSuppressionValue::val,
                    UpdateSuppressionValue::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }
    public static void handlePayload(UpdateSuppressionValue payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;


            Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(
                    ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), ResourceLocation.parse(payload.attribute))
            );

            //System.out.println("setting suppressed value too :"+payload.attribute);
            serverPlayer.getData(ModAttachments.ENTITY_DATA).getAttribute(attributeHolder).setSuppressedValue(payload.val);
            PacketDistributor.sendToPlayer(serverPlayer,new SyncAttributeHolder(serverPlayer.getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder()));
        });
    }
}
