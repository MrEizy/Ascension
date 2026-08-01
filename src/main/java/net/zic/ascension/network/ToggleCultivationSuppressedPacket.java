package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public record ToggleCultivationSuppressedPacket(boolean suppressed) implements CustomPacketPayload {
    public static final Type<ToggleCultivationSuppressedPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "toggle_cultivation_suppressed")
    );

    public static final StreamCodec<FriendlyByteBuf, ToggleCultivationSuppressedPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ToggleCultivationSuppressedPacket decode(FriendlyByteBuf buf) {
                    return new ToggleCultivationSuppressedPacket(buf.readBoolean());
                }

                @Override
                public void encode(FriendlyByteBuf buf, ToggleCultivationSuppressedPacket packet) {
                    buf.writeBoolean(packet.suppressed());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleCultivationSuppressedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            AscensionEntityDataProvider holder = player.getCapability(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
            );

            if (holder == null) {
                return;
            }

            holder.getData(player).setCultivationSuppressed(packet.suppressed());

            player.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        });
    }
}