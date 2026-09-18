package net.zic.ascension.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.visual.SphericalDestructionClientState;

/**
 * Launch payload for the traveling spherical projectile. Sent once, server to
 * nearby clients, at cast time. The client does NOT wait for per-tick position
 * updates — it simulates the same origin+direction*speed*distanceTraveled and
 * radius-growth-over-distance formula the server itself uses for collision
 * (see SphericalDestructionService), so the visual tracks the real projectile
 * without any further network traffic. The server sends the real detonation
 * (SphericalDestructionPayload) separately once it actually hits something.
 */
public record SphericalProjectilePayload(
        Vec3 origin,
        Vec3 direction,
        double speed,
        double startRadius,
        double targetRadius,
        double growthDistance,
        double maxDistance
) implements CustomPacketPayload {

    public static final Type<SphericalProjectilePayload> TYPE =
            new Type<>(AscensionCraft.prefix("spherical_projectile"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SphericalProjectilePayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.origin.x);
                buf.writeDouble(payload.origin.y);
                buf.writeDouble(payload.origin.z);
                buf.writeDouble(payload.direction.x);
                buf.writeDouble(payload.direction.y);
                buf.writeDouble(payload.direction.z);
                buf.writeDouble(payload.speed);
                buf.writeDouble(payload.startRadius);
                buf.writeDouble(payload.targetRadius);
                buf.writeDouble(payload.growthDistance);
                buf.writeDouble(payload.maxDistance);
            },
            buf -> new SphericalProjectilePayload(
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            )
    );

    @Override
    public Type<SphericalProjectilePayload> type() {
        return TYPE;
    }

    /** Runs on the client that received it — starts the local travel simulation. */
    public static void handle(SphericalProjectilePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            SphericalDestructionClientState.get().launch(
                    payload.origin(),
                    payload.direction(),
                    payload.speed(),
                    payload.startRadius(),
                    payload.targetRadius(),
                    payload.growthDistance(),
                    payload.maxDistance(),
                    0xFF6A1B
            );
        });
    }
}