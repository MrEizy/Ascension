package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastingInstance;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

public record SyncCastingInstance(ResourceLocation skill, ICastData castData)implements CustomPacketPayload {

    public static final Type<SyncCastingInstance> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_casting_instance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCastingInstance> STREAM_CODEC =
            StreamCodec.of(SyncCastingInstance::encode, SyncCastingInstance::decode);


    public static void encode(RegistryFriendlyByteBuf buf,SyncCastingInstance packet){
        buf.writeBoolean(packet.skill != null);
        if(packet.skill != null){
            ByteBufUtil.encodeString(buf,packet.skill.toString());
        }
        buf.writeBoolean(packet.castData != null);
        if(packet.castData != null){
            packet.castData.encode(buf);
        }

    }
    public static SyncCastingInstance decode(RegistryFriendlyByteBuf buf){
        ResourceLocation skill = null;
        if(buf.readBoolean()){
            skill = ByteBufUtil.readResourceLocation(buf);
        }
        ICastData castData = null;
        if(buf.readBoolean()){
            castData = ((ICastableSkill) AscensionRegistries.Skills.SKILL_REGISTRY.get(skill)).castDataFromNetwork(buf);
        }
        return new SyncCastingInstance(skill,castData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(SyncCastingInstance payload, IPayloadContext context) {
        context.enqueueWork(()->{
            CastingInstance castingInstance = context.player().getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().getCastingInstance();

            //System.out.println("trying to sync cast instance");
            castingInstance.startClientCast(context.player(),payload.skill);
            castingInstance.setCastData(payload.castData);

        });
    }
}
