package net.zic.ascension.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.passive.DualWielding;

public record OffhandAttackPayload(int targetId) implements CustomPacketPayload {
    public static final Type<OffhandAttackPayload> TYPE = new Type<>(AscensionCraft.prefix("offhand_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OffhandAttackPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeVarInt(payload.targetId),
            buf -> new OffhandAttackPayload(buf.readVarInt())
    );

    @Override
    public Type<OffhandAttackPayload> type() {
        return TYPE;
    }

    public static void handle(OffhandAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DualWielding.offhandAttack(player, payload.targetId());
            }
        });
    }
}