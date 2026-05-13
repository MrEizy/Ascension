package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.qi;

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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.supressors.UpdateSuppressionValue;

public record SyncQi(double qi) implements CustomPacketPayload {

    public static final Type<SyncQi> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sync_qi"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncQi> STREAM_CODEC =
            StreamCodec.composite(

                    ByteBufCodecs.DOUBLE,
                    SyncQi::qi,
                    SyncQi::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }
    public static void handlePayload(SyncQi payload, IPayloadContext context) {
        context.enqueueWork(() -> {
           context.player().getData(ModAttachments.ENTITY_DATA).getQiContainer().setCurrentQi(payload.qi);
        });
    }
}