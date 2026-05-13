package net.zic.ascension.refactor_packages.network.server_bound.skills;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;

public record ClearSlot(int slot)implements CustomPacketPayload {
    public static final Type<ClearSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"clear_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClearSlot::slot,
            ClearSlot::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(ClearSlot payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            entityData.getSkillCastHandler().getHotBar().unSlotSkill(entityData,payload.slot);
            entityData.getSkillCastHandler().getHotBar().syncSlots(context.player());
        });
    }
}
