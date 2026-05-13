package net.thejadeproject.ascension.refactor_packages.network.server_bound.skills;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;

public record SetActiveSlot(int slot)implements CustomPacketPayload {
    public static final Type<SetActiveSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"set_active_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetActiveSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SetActiveSlot::slot,

            SetActiveSlot::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SetActiveSlot payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            entityData.getSkillCastHandler().setSelectedSkill(entityData,payload.slot);

        });
    }
}