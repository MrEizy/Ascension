package net.thejadeproject.ascension.refactor_packages.network.client_bound.mob_culti;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;

public record SyncMobCultivation(
        int entityId,
        String realmId,
        int stage,
        boolean initialized
) implements CustomPacketPayload {

    public static final Type<SyncMobCultivation> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sync_mob_rank"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMobCultivation> STREAM_CODEC =
            StreamCodec.of(SyncMobCultivation::encode, SyncMobCultivation::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SyncMobCultivation packet) {
        buf.writeInt(packet.entityId);
        buf.writeUtf(packet.realmId);
        buf.writeInt(packet.stage);
        buf.writeBoolean(packet.initialized);
    }

    private static SyncMobCultivation decode(RegistryFriendlyByteBuf buf) {
        return new SyncMobCultivation(
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void handlePayload(SyncMobCultivation payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(payload.entityId);
            if (!(entity instanceof LivingEntity living)) return;

            MobCultivationData data = living.getData(ModAttachments.MOB_RANK);
            if (data == null) return;

            data.setRealmId(payload.realmId());
            data.setStage(payload.stage());
            data.setInitialized(payload.initialized());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}