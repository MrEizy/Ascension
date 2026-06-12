package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;

public record SelectSkillSlotPacket(int slot) implements CustomPacketPayload {
    public static final Type<SelectSkillSlotPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "select_skill_slot")
    );

    public static final StreamCodec<FriendlyByteBuf, SelectSkillSlotPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SelectSkillSlotPacket decode(FriendlyByteBuf buf) {
                    return new SelectSkillSlotPacket(buf.readVarInt());
                }

                @Override
                public void encode(FriendlyByteBuf buf, SelectSkillSlotPacket packet) {
                    buf.writeVarInt(packet.slot());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectSkillSlotPacket packet, IPayloadContext context) {
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

            handler.select(packet.slot());
            handler.resolve();
        });
    }
}
