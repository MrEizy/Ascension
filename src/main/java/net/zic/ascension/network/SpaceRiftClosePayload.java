package net.zic.ascension.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.SpaceRiftClientState;

import java.util.UUID;

/** Tells a client to start closing a rift previously opened with {@link SpaceRiftPayload}. */
public record SpaceRiftClosePayload(UUID riftId) implements CustomPacketPayload {

    public static final Type<SpaceRiftClosePayload> TYPE = new Type<>(AscensionCraft.prefix("space_rift_close"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpaceRiftClosePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUUID(payload.riftId),
            buf -> new SpaceRiftClosePayload(buf.readUUID())
    );

    @Override
    public Type<SpaceRiftClosePayload> type() {
        return TYPE;
    }

    public static void handle(SpaceRiftClosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SpaceRiftClientState.get().close(payload.riftId()));
    }
}
