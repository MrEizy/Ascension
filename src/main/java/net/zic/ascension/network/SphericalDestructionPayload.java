package net.zic.ascension.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.SphericalDestructionClientState;

import java.util.UUID;

public record SphericalDestructionPayload(
        UUID projectileId,
        Vec3 center,
        int radius,
        float travelRadius,
        int impactAgeTicks,
        double soundRange,
        int color,
        Identifier dimension
) implements CustomPacketPayload {

    private static final Identifier SOUND_ID = AscensionCraft.prefix("spherical_destruction");

    public static final Type<SphericalDestructionPayload> TYPE =
            new Type<>(AscensionCraft.prefix("spherical_destruction"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SphericalDestructionPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.projectileId);
                buf.writeDouble(payload.center.x);
                buf.writeDouble(payload.center.y);
                buf.writeDouble(payload.center.z);
                buf.writeVarInt(payload.radius);
                buf.writeFloat(payload.travelRadius);
                buf.writeVarInt(payload.impactAgeTicks);
                buf.writeDouble(payload.soundRange);
                buf.writeInt(payload.color);
                buf.writeIdentifier(payload.dimension);
            },
            buf -> new SphericalDestructionPayload(
                    buf.readUUID(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readDouble(),
                    buf.readInt(),
                    buf.readIdentifier()
            )
    );

    @Override
    public Type<SphericalDestructionPayload> type() {
        return TYPE;
    }

    public static void handle(SphericalDestructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !minecraft.level.dimension().identifier().equals(payload.dimension())) {
                return;
            }
            SphericalDestructionClientState.get().impact(
                    payload.projectileId(), payload.dimension(), payload.center(), payload.radius(),
                    payload.travelRadius(), payload.impactAgeTicks(), payload.color()
            );
            if (minecraft.player != null && payload.impactAgeTicks() <= 3
                    && minecraft.player.position().distanceToSqr(payload.center()) <= payload.soundRange() * payload.soundRange()) {
                minecraft.level.playLocalSound(
                        payload.center().x, payload.center().y, payload.center().z,
                        SoundEvent.createVariableRangeEvent(SOUND_ID),
                        SoundSource.HOSTILE, 4.0F, 1.0F, false
                );
            }
        });
    }
}
