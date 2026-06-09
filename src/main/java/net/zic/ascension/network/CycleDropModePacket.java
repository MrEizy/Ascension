package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;

/**
 * Sent client → server when the player presses the cycle-drop-mode keybind.
 *
 * Register in RegisterPayloadHandlersEvent:
    registrar.playToServer(
        CycleDropModePacket.TYPE,
        CycleDropModePacket.STREAM_CODEC,
        CycleDropModePacket::handle);
 */
public record CycleDropModePacket() implements CustomPacketPayload {

    public static final Type<CycleDropModePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AscensionCraft.MOD_ID, "cycle_mode"));

    public static final StreamCodec<FriendlyByteBuf, CycleDropModePacket> STREAM_CODEC =
            StreamCodec.unit(new CycleDropModePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CycleDropModePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof BaseTabletOfDestruction tabletItem) {
                    tabletItem.cycleDropMode(stack, player);
                    return;
                }
            }
        });
    }
}