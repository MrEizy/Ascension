package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.castable.SwordFlightPhysics;

import java.util.UUID;

public record SwordFlightStatePacket(UUID playerId, boolean active) implements CustomPacketPayload {
    public static final Type<SwordFlightStatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_flight_state")
    );

    public static final StreamCodec<FriendlyByteBuf, SwordFlightStatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SwordFlightStatePacket decode(FriendlyByteBuf buf) {
            return new SwordFlightStatePacket(buf.readUUID(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, SwordFlightStatePacket packet) {
            buf.writeUUID(packet.playerId());
            buf.writeBoolean(packet.active());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SwordFlightStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> SwordFlightPhysics.setClientFlightState(packet.playerId(), packet.active()));
    }
}
