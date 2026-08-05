package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.passive.WeaponMasteryService;

/** Client attack intent; all eligibility, resource and damage decisions remain server-side. */
public record WeaponSwingRequestPacket() implements CustomPacketPayload {
    public static final Type<WeaponSwingRequestPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "weapon_swing_request")
    );
    public static final StreamCodec<FriendlyByteBuf, WeaponSwingRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new WeaponSwingRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WeaponSwingRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WeaponMasteryService.trySwing(player);
            }
        });
    }
}
