package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.AscensionGui;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.ArrayList;
import java.util.List;

public record OpenStarterSelectionPacket(
        StarterSelectionStage stage,
        List<Identifier> options,
        Identifier selectedBloodline
) implements CustomPacketPayload {
    public static final Type<OpenStarterSelectionPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "open_starter_selection")
    );

    public static final StreamCodec<FriendlyByteBuf, OpenStarterSelectionPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenStarterSelectionPacket decode(FriendlyByteBuf buf) {
                    StarterSelectionStage stage = buf.readEnum(StarterSelectionStage.class);
                    int size = buf.readVarInt();
                    List<Identifier> options = new ArrayList<>(size);
                    for (int index = 0; index < size; index++) {
                        options.add(ByteBufHelpers.decodeIdentifier(buf));
                    }

                    Identifier selectedBloodline = buf.readBoolean()
                            ? ByteBufHelpers.decodeIdentifier(buf)
                            : null;

                    return new OpenStarterSelectionPacket(stage, options, selectedBloodline);
                }

                @Override
                public void encode(FriendlyByteBuf buf, OpenStarterSelectionPacket packet) {
                    buf.writeEnum(packet.stage());
                    buf.writeVarInt(packet.options().size());
                    for (Identifier option : packet.options()) {
                        ByteBufHelpers.encodeIdentifier(option, buf);
                    }

                    buf.writeBoolean(packet.selectedBloodline() != null);
                    if (packet.selectedBloodline() != null) {
                        ByteBufHelpers.encodeIdentifier(packet.selectedBloodline(), buf);
                    }
                }
            };

    public OpenStarterSelectionPacket {
        options = List.copyOf(options);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenStarterSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AscensionGui.openStarterSelection(
                packet.stage(),
                packet.options(),
                packet.selectedBloodline()
        ));
    }
}
