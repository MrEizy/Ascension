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

/**
 * Detonation (impact) payload — fires the actual shockwave visual + sound at
 * the final position. Server-authoritative: sent once the traveling sphere
 * (see SphericalProjectilePayload) actually hits something.
 *
 * IMPORTANT: this must be registered with registrar.playToClient(...), not
 * playToServer(...) — it was registered the wrong direction in
 * AscensionCraft.registerPayloads(), which is the actual reason the shader
 * never fired (the server was never receiving anything back, and this
 * handler never ran on any client). Swap that one line.
 *
 * TODO: SOUND_ID has no matching sounds.json/.ogg yet — point it at a real
 * resource or an existing Ascension impact sound.
 */
public record SphericalDestructionPayload(Vec3 center, int radius, Identifier dimension)
        implements CustomPacketPayload {

    private static final Identifier SOUND_ID = AscensionCraft.prefix("spherical_destruction");

    public static final Type<SphericalDestructionPayload> TYPE =
            new Type<>(AscensionCraft.prefix("spherical_destruction"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SphericalDestructionPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.center.x);
                buf.writeDouble(payload.center.y);
                buf.writeDouble(payload.center.z);
                buf.writeVarInt(payload.radius);
                buf.writeIdentifier(payload.dimension);
            },
            buf -> new SphericalDestructionPayload(
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readVarInt(),
                    buf.readIdentifier()
            )
    );

    @Override
    public Type<SphericalDestructionPayload> type() {
        return TYPE;
    }

    /** Runs on the client that received it — starts the impact wave and plays the sound. */
    public static void handle(SphericalDestructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            SphericalDestructionClientState.get().impact(payload.center(), payload.radius(), 0xFF6A1B);
            mc.level.playLocalSound(
                    payload.center().x, payload.center().y, payload.center().z,
                    SoundEvent.createVariableRangeEvent(SOUND_ID),
                    SoundSource.HOSTILE,
                    4.0F,
                    1.0F,
                    false
            );
        });
    }
}