package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.particle.ParticleFieldController;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.UUID;

public record ActiveCastVisualStatePacket(
        UUID playerId,
        Identifier skillId,
        int stage,
        float progress
) implements CustomPacketPayload {
    public static final Type<ActiveCastVisualStatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "active_cast_visual_state")
    );

    public static final StreamCodec<FriendlyByteBuf, ActiveCastVisualStatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ActiveCastVisualStatePacket decode(FriendlyByteBuf buf) {
            UUID playerId = buf.readUUID();
            Identifier skillId = buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null;
            int stage = buf.readVarInt();
            float progress = buf.readFloat();
            return new ActiveCastVisualStatePacket(playerId, skillId, stage, progress);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ActiveCastVisualStatePacket packet) {
            buf.writeUUID(packet.playerId());
            buf.writeBoolean(packet.skillId() != null);
            if (packet.skillId() != null) {
                ByteBufHelpers.encodeIdentifier(packet.skillId(), buf);
            }
            buf.writeVarInt(Math.max(0, packet.stage()));
            buf.writeFloat(Math.clamp(packet.progress(), 0.0F, 1.0F));
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ActiveCastVisualStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ParticleFieldController.updateCast(
                packet.playerId(),
                packet.skillId(),
                packet.stage(),
                packet.progress()
        ));
    }
}
