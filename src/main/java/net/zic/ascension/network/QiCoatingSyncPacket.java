package net.zic.ascension.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.client.visual.QiCoatingClientState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * The Qi Coating an entity has on a channel (QiCoatingManager), or null when it has none, and the game tick it went on, so
 * every client grows it in from the same moment.
 */
@NullMarked
public record QiCoatingSyncPacket(UUID entity, QiCoatingChannel channel, @Nullable Identifier coating, long startTick)
        implements CustomPacketPayload {
    public static final Type<QiCoatingSyncPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "qi_coating_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, QiCoatingSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public QiCoatingSyncPacket decode(FriendlyByteBuf buf) {
            UUID entity = UUIDUtil.STREAM_CODEC.decode(buf);
            QiCoatingChannel channel = QiCoatingChannel.STREAM_CODEC.decode(buf);
            Identifier coating = buf.readBoolean() ? buf.readIdentifier() : null;
            return new QiCoatingSyncPacket(entity, channel, coating, coating == null ? 0L : buf.readVarLong());
        }

        @Override
        public void encode(FriendlyByteBuf buf, QiCoatingSyncPacket packet) {
            UUIDUtil.STREAM_CODEC.encode(buf, packet.entity());
            QiCoatingChannel.STREAM_CODEC.encode(buf, packet.channel());
            buf.writeBoolean(packet.coating() != null);
            if (packet.coating() != null) {
                buf.writeIdentifier(packet.coating());
                buf.writeVarLong(packet.startTick());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QiCoatingSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> QiCoatingClientState.accept(packet.entity(), packet.channel(), packet.coating(), packet.startTick()));
    }
}
