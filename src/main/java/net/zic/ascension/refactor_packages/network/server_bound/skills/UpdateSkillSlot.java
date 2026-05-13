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
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;
import net.zic.ascension.refactor_packages.skills.castable.ICastableSkill;

public record UpdateSkillSlot(int slot, String skill)implements CustomPacketPayload {
    public static final Type<UpdateSkillSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"update_skill_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSkillSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateSkillSlot::slot,
            ByteBufCodecs.STRING_UTF8,
            UpdateSkillSlot::skill,
            UpdateSkillSlot::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(UpdateSkillSlot payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            if(AscensionRegistries.Skills.SKILL_REGISTRY.get(ResourceLocation.parse(payload.skill)) instanceof ICastableSkill castableSkill){
                entityData.getSkillCastHandler().getHotBar().slotSkill(entityData,ResourceLocation.parse(payload.skill),payload.slot);
                entityData.getSkillCastHandler().getHotBar().syncSlots(context.player());
            }

        });
    }
}
