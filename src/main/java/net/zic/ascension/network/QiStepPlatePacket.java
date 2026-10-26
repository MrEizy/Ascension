package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.QiStepClient;
import org.jspecify.annotations.NullMarked;

/**
 * Shows a Qi Steps foothold another player just formed, at `plate` facing `yaw` (their body's, in degrees): under one foot
 * there, or with `bothFeet`, under both feet either side of it.
 */
@NullMarked
public record QiStepPlatePacket(Vec3 plate, float yaw, boolean bothFeet) implements CustomPacketPayload {
    public static final Type<QiStepPlatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "qi_step_plate")
    );

    public static final StreamCodec<FriendlyByteBuf, QiStepPlatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public QiStepPlatePacket decode(FriendlyByteBuf buf) {
            return new QiStepPlatePacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(),
                    buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, QiStepPlatePacket packet) {
            buf.writeDouble(packet.plate().x);
            buf.writeDouble(packet.plate().y);
            buf.writeDouble(packet.plate().z);
            buf.writeFloat(packet.yaw());
            buf.writeBoolean(packet.bothFeet());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QiStepPlatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> QiStepClient.addFootholds(packet.plate(), packet.yaw(), packet.bothFeet()));
    }
}
