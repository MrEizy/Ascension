package net.zic.ascension.network.alchemy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.hud.CultivationProgressBar;

public record ClientboundMedicinalCultivationProgressPacket(Identifier pathId, float progress)
        implements CustomPacketPayload {
    public static final Type<ClientboundMedicinalCultivationProgressPacket> TYPE =
            new Type<>(AscensionCraft.prefix("medicinal_cultivation_progress"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundMedicinalCultivationProgressPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeIdentifier(packet.pathId());
                        buffer.writeFloat(packet.progress());
                    },
                    buffer -> new ClientboundMedicinalCultivationProgressPacket(
                            buffer.readIdentifier(), buffer.readFloat())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundMedicinalCultivationProgressPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> CultivationProgressBar.showMedicinalProgress(packet.pathId(), packet.progress()));
    }
}
