package net.zic.ascension.network.tribulation;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.tribulation.client.ClientTribulationManager;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

//only contains the parts that changed since the last sync
public record ClientboundTribulationUpdatePacket(
        UUID id,
        @Nullable Vec3 position,
        @Nullable ByteBuf data
) implements CustomPacketPayload {
    public static final Type<ClientboundTribulationUpdatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation_update")
    );

    public static final StreamCodec<FriendlyByteBuf, ClientboundTribulationUpdatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.id());
                buf.writeBoolean(packet.position() != null);
                if (packet.position() != null) TribulationPacketHelper.writeVec3(buf, packet.position());
                buf.writeBoolean(packet.data() != null);
                if (packet.data() != null) TribulationPacketHelper.writeData(buf, packet.data());
            },
            buf -> new ClientboundTribulationUpdatePacket(
                    buf.readUUID(),
                    buf.readBoolean() ? TribulationPacketHelper.readVec3(buf) : null,
                    buf.readBoolean() ? TribulationPacketHelper.readData(buf) : null
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundTribulationUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTribulationManager.update(packet));
    }
}
