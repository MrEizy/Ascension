package net.zic.ascension.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.combat.DualWieldClientState;

public record OffhandCooldownPayload(int ticks) implements CustomPacketPayload {
    public static final Type<OffhandCooldownPayload> TYPE = new Type<>(AscensionCraft.prefix("offhand_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OffhandCooldownPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeVarInt(payload.ticks),
            buf -> new OffhandCooldownPayload(buf.readVarInt())
    );

    @Override
    public Type<OffhandCooldownPayload> type() {
        return TYPE;
    }

    public static void handle(OffhandCooldownPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> DualWieldClientState.setCooldown(payload.ticks()));
    }
}