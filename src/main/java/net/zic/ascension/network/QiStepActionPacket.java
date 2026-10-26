package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.passive.QiStepService;
import org.jspecify.annotations.NullMarked;

/**
 * Sent by a player's client each time Qi Steps forms a plate under them: a step, a jump off a plate, or a charge of standing
 * still on one. The server pays for it and shows the plate (at `plate`) to everyone nearby.
 */
@NullMarked
public record QiStepActionPacket(QiStepService.Action action, Vec3 plate) implements CustomPacketPayload {
    public static final Type<QiStepActionPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "qi_step_action")
    );

    public static final StreamCodec<FriendlyByteBuf, QiStepActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public QiStepActionPacket decode(FriendlyByteBuf buf) {
            QiStepService.Action[] actions = QiStepService.Action.values();
            int index = buf.readVarInt();
            QiStepService.Action action = index >= 0 && index < actions.length ? actions[index] : QiStepService.Action.STEP;
            return new QiStepActionPacket(action, new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void encode(FriendlyByteBuf buf, QiStepActionPacket packet) {
            buf.writeVarInt(packet.action().ordinal());
            buf.writeDouble(packet.plate().x);
            buf.writeDouble(packet.plate().y);
            buf.writeDouble(packet.plate().z);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QiStepActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QiStepService.handleAction(player, packet.action(), packet.plate());
            }
        });
    }
}
