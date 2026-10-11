package net.zic.ascension.network.tribulation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.tribulation.ClientTribulationManager;

import java.util.UUID;

//sent when a player starts tracking a tribulation
public record ClientboundTribulationAddPacket(
        UUID id,
        Identifier tribulation,
        ResourceKey<Level> dimension,
        Vec3 position,
        byte[] data
) implements CustomPacketPayload {
    public static final Type<ClientboundTribulationAddPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation_add")
    );

    public static final StreamCodec<FriendlyByteBuf, ClientboundTribulationAddPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.id());
                buf.writeIdentifier(packet.tribulation());
                buf.writeResourceKey(packet.dimension());
                TribulationPacketHelper.writeVec3(buf, packet.position());
                buf.writeByteArray(packet.data());
            },
            buf -> new ClientboundTribulationAddPacket(
                    buf.readUUID(),
                    buf.readIdentifier(),
                    buf.readResourceKey(Registries.DIMENSION),
                    TribulationPacketHelper.readVec3(buf),
                    buf.readByteArray()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundTribulationAddPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTribulationManager.add(packet));
    }
}
