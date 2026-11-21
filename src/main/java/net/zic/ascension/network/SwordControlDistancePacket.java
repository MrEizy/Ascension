package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.castable.SwordControlService;

public record SwordControlDistancePacket(int steps) implements CustomPacketPayload {
    public static final Type<SwordControlDistancePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_control_distance")
    );

    public static final StreamCodec<FriendlyByteBuf, SwordControlDistancePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SwordControlDistancePacket decode(FriendlyByteBuf buf) {
            return new SwordControlDistancePacket(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, SwordControlDistancePacket packet) {
            buf.writeVarInt(packet.steps());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SwordControlDistancePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SwordControlService.adjustDistance(player, packet.steps());
            }
        });
    }
}
