package net.zic.ascension.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.DivineSenseClientState;

import java.util.List;

public record ClientboundDivineSensePacket(
        Vec3 center,
        float radius,
        int durationTicks,
        int color,
        List<Integer> entityIds
) implements CustomPacketPayload {

    public static final Type<ClientboundDivineSensePacket> TYPE =
            new Type<>(AscensionCraft.prefix("divine_sense"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDivineSensePacket> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(ClientboundDivineSensePacket::writeVec3, ClientboundDivineSensePacket::readVec3), ClientboundDivineSensePacket::center,
            ByteBufCodecs.FLOAT, ClientboundDivineSensePacket::radius,
            ByteBufCodecs.VAR_INT, ClientboundDivineSensePacket::durationTicks,
            ByteBufCodecs.INT, ClientboundDivineSensePacket::color,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), ClientboundDivineSensePacket::entityIds,
            ClientboundDivineSensePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundDivineSensePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            DivineSenseClientState.get().start(packet.center(), packet.radius(), packet.durationTicks(), packet.color(), packet.entityIds());
        });
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundDivineSensePacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    private static void writeVec3(ByteBuf buf, Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    private static Vec3 readVec3(ByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}