package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.client.visual.RuntimeVisualAction;
import net.zic.ascension.api.client.visual.RuntimeVisualKind;
import net.zic.ascension.api.client.visual.RuntimeVisualLink;
import net.zic.ascension.api.client.visual.RuntimeVisualState;
import net.zic.ascension.client.visual.ClientRuntimeVisualManager;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RuntimeVisualPacket(
        RuntimeVisualAction action,
        RuntimeVisualState state
) implements CustomPacketPayload {
    public static final Type<RuntimeVisualPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "runtime_visual")
    );

    public static final StreamCodec<FriendlyByteBuf, RuntimeVisualPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RuntimeVisualPacket decode(FriendlyByteBuf buf) {
            RuntimeVisualAction action = buf.readEnum(RuntimeVisualAction.class);
            UUID runtimeId = buf.readUUID();
            RuntimeVisualKind kind = buf.readEnum(RuntimeVisualKind.class);
            Identifier visual = buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null;
            UUID ownerId = buf.readBoolean() ? buf.readUUID() : null;
            Vec3 position = readVec3(buf);
            Vec3 velocity = readVec3(buf);
            int pointCount = Math.clamp(buf.readVarInt(), 0, 64);
            List<Vec3> points = new ArrayList<>(pointCount);
            for (int index = 0; index < pointCount; index++) {
                points.add(readVec3(buf));
            }
            int linkCount = Math.clamp(buf.readVarInt(), 0, 128);
            List<RuntimeVisualLink> links = new ArrayList<>(linkCount);
            for (int index = 0; index < linkCount; index++) {
                links.add(new RuntimeVisualLink(buf.readVarInt(), buf.readVarInt()));
            }
            long expiresAt = buf.readLong();
            int stage = buf.readVarInt();
            int flags = buf.readVarInt();
            float progress = buf.readFloat();
            long seed = buf.readLong();
            double primary = buf.readDouble();
            double secondary = buf.readDouble();
            return new RuntimeVisualPacket(
                    action,
                    new RuntimeVisualState(
                            runtimeId,
                            kind,
                            visual,
                            ownerId,
                            position,
                            velocity,
                            points,
                            links,
                            expiresAt,
                            stage,
                            flags,
                            progress,
                            seed,
                            primary,
                            secondary
                    )
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, RuntimeVisualPacket packet) {
            RuntimeVisualState state = packet.state();
            buf.writeEnum(packet.action());
            buf.writeUUID(state.runtimeId());
            buf.writeEnum(state.kind());
            buf.writeBoolean(state.visual() != null);
            if (state.visual() != null) {
                ByteBufHelpers.encodeIdentifier(state.visual(), buf);
            }
            buf.writeBoolean(state.ownerId() != null);
            if (state.ownerId() != null) {
                buf.writeUUID(state.ownerId());
            }
            writeVec3(buf, state.position());
            writeVec3(buf, state.velocity());
            int pointCount = Math.min(64, state.points().size());
            buf.writeVarInt(pointCount);
            for (int index = 0; index < pointCount; index++) {
                writeVec3(buf, state.points().get(index));
            }
            int linkCount = Math.min(128, state.links().size());
            buf.writeVarInt(linkCount);
            for (int index = 0; index < linkCount; index++) {
                RuntimeVisualLink link = state.links().get(index);
                buf.writeVarInt(link.from());
                buf.writeVarInt(link.to());
            }
            buf.writeLong(state.expiresAt());
            buf.writeVarInt(Math.max(0, state.stage()));
            buf.writeVarInt(Math.max(0, state.flags()));
            buf.writeFloat(Math.clamp(state.progress(), 0.0F, 1.0F));
            buf.writeLong(state.seed());
            buf.writeDouble(state.primaryValue());
            buf.writeDouble(state.secondaryValue());
        }

        private Vec3 readVec3(FriendlyByteBuf buf) {
            return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        private void writeVec3(FriendlyByteBuf buf, Vec3 value) {
            Vec3 resolved = value == null ? Vec3.ZERO : value;
            buf.writeDouble(resolved.x);
            buf.writeDouble(resolved.y);
            buf.writeDouble(resolved.z);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RuntimeVisualPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientRuntimeVisualManager.accept(packet.action(), packet.state()));
    }
}
