package net.zic.ascension.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.castable.VoidPocketService;

import java.util.UUID;

/** Sent when a player clicks into the hole of an interactable rift. The server re-checks everything. */
public record SpaceRiftInteractPayload(UUID riftId) implements CustomPacketPayload {

    public static final Type<SpaceRiftInteractPayload> TYPE = new Type<>(AscensionCraft.prefix("space_rift_interact"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpaceRiftInteractPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUUID(payload.riftId),
            buf -> new SpaceRiftInteractPayload(buf.readUUID())
    );

    @Override
    public Type<SpaceRiftInteractPayload> type() {
        return TYPE;
    }

    public static void handle(SpaceRiftInteractPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                VoidPocketService.interact(player, payload.riftId());
            }
        });
    }
}
