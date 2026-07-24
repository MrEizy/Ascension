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

public record ParticleFieldStatePacket(
        UUID playerId,
        Identifier skillId
) implements CustomPacketPayload {
    public static final Type<ParticleFieldStatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "particle_field_state")
    );

    public static final StreamCodec<FriendlyByteBuf, ParticleFieldStatePacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ParticleFieldStatePacket decode(FriendlyByteBuf buf) {
                    UUID playerId = buf.readUUID();
                    Identifier skillId = buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null;
                    return new ParticleFieldStatePacket(playerId, skillId);
                }

                @Override
                public void encode(FriendlyByteBuf buf, ParticleFieldStatePacket packet) {
                    buf.writeUUID(packet.playerId());
                    buf.writeBoolean(packet.skillId() != null);
                    if (packet.skillId() != null) {
                        ByteBufHelpers.encodeIdentifier(packet.skillId(), buf);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ParticleFieldStatePacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> ParticleFieldController.updateRemote(packet.playerId(), packet.skillId())
        );
    }
}
