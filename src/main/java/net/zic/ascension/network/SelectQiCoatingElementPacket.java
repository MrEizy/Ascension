package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.impl.core.skill.castable.QiCoatingSelectionService;
import org.jspecify.annotations.NullMarked;

/**
 * Sent when the player picks an element in the coating wheel for one of the Qi Coating skills.
 */
@NullMarked
public record SelectQiCoatingElementPacket(QiCoatingChannel channel, Identifier element) implements CustomPacketPayload {
    public static final Type<SelectQiCoatingElementPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "select_qi_coating_element")
    );

    public static final StreamCodec<FriendlyByteBuf, SelectQiCoatingElementPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SelectQiCoatingElementPacket decode(FriendlyByteBuf buf) {
                    return new SelectQiCoatingElementPacket(QiCoatingChannel.STREAM_CODEC.decode(buf), buf.readIdentifier());
                }

                @Override
                public void encode(FriendlyByteBuf buf, SelectQiCoatingElementPacket packet) {
                    QiCoatingChannel.STREAM_CODEC.encode(buf, packet.channel());
                    buf.writeIdentifier(packet.element());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectQiCoatingElementPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QiCoatingSelectionService.select(player, packet.channel(), packet.element());
            }
        });
    }
}
