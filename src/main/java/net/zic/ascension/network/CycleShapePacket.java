package net.zic.ascension.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;

public record CycleShapePacket() implements CustomPacketPayload {

    public static final Type<CycleShapePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cycle_shape"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, CycleShapePacket> STREAM_CODEC =
            StreamCodec.unit(new CycleShapePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CycleShapePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (main.getItem() instanceof TabletOfDestructionAscendant ascendant) {
                ascendant.cycleShape(main, player);
            }
        });
    }
}