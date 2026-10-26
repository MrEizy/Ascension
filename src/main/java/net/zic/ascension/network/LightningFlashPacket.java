package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.layer.QiCoatingLayer;
import org.jspecify.annotations.NullMarked;

/**
 * Tells the flashing player and everyone tracking them that a Lightning Step spear flash just started, heading along the
 * horizontal direction (dirX, dirZ). The flashing player's client moves them; every client draws the trail.
 */
@NullMarked
public record LightningFlashPacket(int entityId, float dirX, float dirZ) implements CustomPacketPayload {
    public static final Type<LightningFlashPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_flash")
    );

    public static final StreamCodec<FriendlyByteBuf, LightningFlashPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LightningFlashPacket decode(FriendlyByteBuf buf) {
            return new LightningFlashPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void encode(FriendlyByteBuf buf, LightningFlashPacket packet) {
            buf.writeVarInt(packet.entityId());
            buf.writeFloat(packet.dirX());
            buf.writeFloat(packet.dirZ());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LightningFlashPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> QiCoatingLayer.startLightningFlash(packet.entityId(), packet.dirX(), packet.dirZ()));
    }
}
