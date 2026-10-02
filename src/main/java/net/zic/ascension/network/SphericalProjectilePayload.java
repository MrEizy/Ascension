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

import java.util.UUID;

public record SphericalProjectilePayload(
        UUID projectileId,
        Identifier dimension,
        Vec3 origin,
        Vec3 direction,
        double speed,
        double startRadius,
        double targetRadius,
        double growthDistance,
        double maxDistance,
        double traveled,
        int color
) implements CustomPacketPayload {

    public static final Type<SphericalProjectilePayload> TYPE =
            new Type<>(AscensionCraft.prefix("spherical_projectile"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SphericalProjectilePayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.projectileId);
                buf.writeIdentifier(payload.dimension);
                writeVec3(buf, payload.origin);
                writeVec3(buf, payload.direction);
                buf.writeDouble(payload.speed);
                buf.writeDouble(payload.startRadius);
                buf.writeDouble(payload.targetRadius);
                buf.writeDouble(payload.growthDistance);
                buf.writeDouble(payload.maxDistance);
                buf.writeDouble(payload.traveled);
                buf.writeInt(payload.color);
            },
            buf -> new SphericalProjectilePayload(
                    buf.readUUID(),
                    buf.readIdentifier(),
                    readVec3(buf),
                    readVec3(buf),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readInt()
            )
    );

    @Override
    public Type<SphericalProjectilePayload> type() {
        return TYPE;
    }

    public static void handle(SphericalProjectilePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !minecraft.level.dimension().identifier().equals(payload.dimension())) {
                return;
            }
            SphericalDestructionClientState.get().launch(
                    payload.projectileId(), payload.dimension(), payload.origin(), payload.direction(),
                    payload.speed(), payload.startRadius(), payload.targetRadius(),
                    payload.growthDistance(), payload.maxDistance(), payload.traveled(), payload.color()
            );
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
