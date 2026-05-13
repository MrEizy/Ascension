package net.zic.ascension.refactor_packages.network.client_bound.entity_data.attributes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;

public record SyncCurrentHealth(double val)implements CustomPacketPayload {

    public static final Type<SyncCurrentHealth> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_current_health"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCurrentHealth> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            SyncCurrentHealth::val,
            SyncCurrentHealth::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncCurrentHealth payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            if(entityData.getHealth() != payload.val)entityData.setHealth(payload.val);
        });
    }
}
