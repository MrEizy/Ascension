package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.QiStepClient;
import net.zic.ascension.impl.core.skill.passive.PassiveModifiers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Tells a player's own client whether Qi Steps is on (with its settings, or null when off), and whether the current trip
 * through the air is spent (out of qi or strain), so no more plates form until they land.
 */
@NullMarked
public record QiStepStatePacket(PassiveModifiers.@Nullable QiStep settings, boolean exhausted) implements CustomPacketPayload {
    public static final Type<QiStepStatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "qi_step_state")
    );

    public static final StreamCodec<FriendlyByteBuf, QiStepStatePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public QiStepStatePacket decode(FriendlyByteBuf buf) {
            PassiveModifiers.QiStep settings = buf.readBoolean()
                    ? new PassiveModifiers.QiStep(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble())
                    : null;
            return new QiStepStatePacket(settings, buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, QiStepStatePacket packet) {
            PassiveModifiers.QiStep settings = packet.settings();
            buf.writeBoolean(settings != null);
            if (settings != null) {
                buf.writeDouble(settings.stepCost());
                buf.writeDouble(settings.jumpCost());
                buf.writeDouble(settings.hoverCost());
                buf.writeVarInt(settings.hoverTicks());
                buf.writeDouble(settings.maxStrain());
                buf.writeDouble(settings.jumpStrain());
                buf.writeDouble(settings.hoverStrain());
                buf.writeDouble(settings.stepDistance());
            }
            buf.writeBoolean(packet.exhausted());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QiStepStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> QiStepClient.setState(packet.settings(), packet.exhausted()));
    }
}
