package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.held.HeldCastVisualPhase;
import net.zic.ascension.client.particle.ParticleFieldController;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.UUID;

public record HeldCastVisualStatePacket(
        UUID playerId,
        Identifier skillId,
        HeldCastVisualPhase phase,
        int stage,
        float charge
) implements CustomPacketPayload {
    public static final Type<HeldCastVisualStatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "held_cast_visual_state")
    );

    public static final StreamCodec<FriendlyByteBuf, HeldCastVisualStatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public HeldCastVisualStatePacket decode(FriendlyByteBuf buf) {
            UUID playerId = buf.readUUID();
            Identifier skillId = buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null;
            HeldCastVisualPhase phase = buf.readEnum(HeldCastVisualPhase.class);
            int stage = buf.readVarInt();
            float charge = buf.readFloat();
            return new HeldCastVisualStatePacket(playerId, skillId, phase, stage, charge);
        }

        @Override
        public void encode(FriendlyByteBuf buf, HeldCastVisualStatePacket packet) {
            buf.writeUUID(packet.playerId());
            buf.writeBoolean(packet.skillId() != null);
            if (packet.skillId() != null) {
                ByteBufHelpers.encodeIdentifier(packet.skillId(), buf);
            }
            buf.writeEnum(packet.phase());
            buf.writeVarInt(Math.max(0, packet.stage()));
            buf.writeFloat(Math.clamp(packet.charge(), 0.0F, 1.0F));
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeldCastVisualStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ParticleFieldController.updateRemoteHeld(
                packet.playerId(),
                packet.skillId(),
                packet.phase(),
                packet.stage(),
                packet.charge()
        ));
    }
}
