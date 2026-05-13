package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public record SyncSlot(int slot, ResourceLocation skill, IPreCastData preCastData) implements CustomPacketPayload {

    public static final Type<SyncSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_skill_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSlot> STREAM_CODEC =
            StreamCodec.of(SyncSlot::encode, SyncSlot::decode);


    public static void encode(RegistryFriendlyByteBuf buf,SyncSlot slot){
        buf.writeInt(slot.slot);
        buf.writeBoolean(slot.skill != null);
        if(slot.skill != null){
            ByteBufUtil.encodeString(buf,slot.skill.toString());
            if(slot.preCastData != null) slot.preCastData.encode(buf);
        }

    }
    public static SyncSlot decode(RegistryFriendlyByteBuf buf){
        int slot = buf.readInt();
        if(buf.readBoolean()) {
            ResourceLocation skill = ByteBufUtil.readResourceLocation(buf);
            IPreCastData preCastData = null;
            if (AscensionRegistries.Skills.SKILL_REGISTRY.get(skill) instanceof ICastableSkill castableSkill) preCastData = castableSkill.preCastDataFromNetwork(buf);
            return new SyncSlot(slot, skill, preCastData);
        }
        return new SyncSlot(slot,null,null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(SyncSlot payload, IPayloadContext context) {
        context.enqueueWork(()->{
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            if(payload.skill == null){
                //System.out.println("un slotting skill");
                entityData.getSkillCastHandler().getHotBar().unSlotSkill(entityData,payload.slot);

            }else{
                entityData.getSkillCastHandler().getHotBar().slotSkill(entityData,payload.skill,payload.slot,payload.preCastData);
            }

        });
    }
}
