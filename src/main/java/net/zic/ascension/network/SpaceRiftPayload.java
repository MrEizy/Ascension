package net.zic.ascension.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.event.SpaceRiftEvents;
import net.zic.ascension.client.visual.SpaceRiftClientState;

import java.util.UUID;

/**
 * Tells a client to tear open a space rift. It stays open until {@link SpaceRiftClosePayload} or its duration runs out.
 * {@code interactable} is only set for the player who may click the rift (see {@link SpaceRiftInteractPayload}).
 */
public record SpaceRiftPayload(
        UUID riftId,
        Identifier dimension,
        Vec3 center,
        Vec3 facing,
        Vec3 upHint,
        float radius,
        float durationSeconds,
        boolean interactable
) implements CustomPacketPayload {

    public static final Type<SpaceRiftPayload> TYPE = new Type<>(AscensionCraft.prefix("space_rift"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpaceRiftPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.riftId);
                buf.writeIdentifier(payload.dimension);
                writeVec3(buf, payload.center);
                writeVec3(buf, payload.facing);
                writeVec3(buf, payload.upHint);
                buf.writeFloat(payload.radius);
                buf.writeFloat(payload.durationSeconds);
                buf.writeBoolean(payload.interactable);
            },
            buf -> new SpaceRiftPayload(
                    buf.readUUID(),
                    buf.readIdentifier(),
                    readVec3(buf),
                    readVec3(buf),
                    readVec3(buf),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<SpaceRiftPayload> type() {
        return TYPE;
    }

    public static void handle(SpaceRiftPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !minecraft.level.dimension().identifier().equals(payload.dimension())) {
                return;
            }
            SpaceRiftClientState.get().open(payload.riftId(), payload.dimension(), payload.center(),
                    payload.facing(), payload.upHint(), payload.radius(), payload.durationSeconds(), payload.interactable());
            SpaceRiftEvents.playOpenSounds(payload.center());
        });
    }

    private static void writeVec3(RegistryFriendlyByteBuf buf, Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    private static Vec3 readVec3(RegistryFriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
