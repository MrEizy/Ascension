package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.castable.QiCoatingMining;
import org.jspecify.annotations.NullMarked;

/**
 * Sent by a player's client (QiCoatingMiningClient) the moment they let go of the use key after charging a till or path with a
 * coated hoe or shovel, so the server changes the blocks at once instead of waiting for the held clicks to stop
 * (QiCoatingMining.releaseArea).
 */
@NullMarked
public record ServerboundToolChargeReleasePacket() implements CustomPacketPayload {
    public static final Type<ServerboundToolChargeReleasePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tool_charge_release")
    );

    public static final StreamCodec<FriendlyByteBuf, ServerboundToolChargeReleasePacket> STREAM_CODEC =
            StreamCodec.unit(new ServerboundToolChargeReleasePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundToolChargeReleasePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QiCoatingMining.releaseArea(player);
            }
        });
    }
}
