package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.starter.StarterSelectionManager;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.zenithlib.network.ByteBufHelpers;

public record ChooseStarterOptionPacket(
        StarterSelectionStage stage,
        Identifier selectedId
) implements CustomPacketPayload {
    public static final Type<ChooseStarterOptionPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "choose_starter_option")
    );

    public static final StreamCodec<FriendlyByteBuf, ChooseStarterOptionPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ChooseStarterOptionPacket decode(FriendlyByteBuf buf) {
                    return new ChooseStarterOptionPacket(
                            buf.readEnum(StarterSelectionStage.class),
                            ByteBufHelpers.decodeIdentifier(buf)
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, ChooseStarterOptionPacket packet) {
                    buf.writeEnum(packet.stage());
                    ByteBufHelpers.encodeIdentifier(packet.selectedId(), buf);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChooseStarterOptionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            StarterSelectionManager.handleChoice(
                    player,
                    packet.stage(),
                    packet.selectedId()
            );
        });
    }
}
