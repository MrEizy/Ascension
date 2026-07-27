package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.zenithlib.network.ByteBufHelpers;

public record UpdateSkillSlotPacket(int slot, Identifier skill) implements CustomPacketPayload {
    public static final Type<UpdateSkillSlotPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "update_skill_slot")
    );

    public static final StreamCodec<FriendlyByteBuf, UpdateSkillSlotPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public UpdateSkillSlotPacket decode(FriendlyByteBuf buf) {
                    int slot = buf.readVarInt();
                    Identifier skill = buf.readBoolean()
                            ? ByteBufHelpers.decodeIdentifier(buf)
                            : null;
                    return new UpdateSkillSlotPacket(slot, skill);
                }

                @Override
                public void encode(FriendlyByteBuf buf, UpdateSkillSlotPacket packet) {
                    buf.writeVarInt(packet.slot());
                    buf.writeBoolean(packet.skill() != null);
                    if (packet.skill() != null) {
                        ByteBufHelpers.encodeIdentifier(packet.skill(), buf);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSkillSlotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            SkillCastHandler handler = player.getData(
                    AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
            );
            if (packet.slot() < 0 || packet.slot() >= handler.getMaxSlots()) {
                return;
            }

            Identifier skillId = packet.skill();
            if (skillId == null) {
                handler.slotSkill(null, packet.slot());
                handler.resolve();
                return;
            }

            AscensionEntityDataProvider dataHolder = player.getCapability(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
            );
            if (dataHolder == null) {
                return;
            }

            OriginSource source = dataHolder.getData(player).getSource();
            if (!AscensionOriginSourceHelper.hasSkill(source,skillId)) {
                return;
            }
            if (!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    player.registryAccess()
            ) instanceof CastableSkill)) {
                return;
            }

            for (int index = 0; index < handler.getMaxSlots(); index++) {
                if (index != packet.slot() && skillId.equals(handler.getSkill(index))) {
                    handler.slotSkill(null, index);
                }
            }
            handler.slotSkill(skillId, packet.slot());
            handler.resolve();
        });
    }
}
