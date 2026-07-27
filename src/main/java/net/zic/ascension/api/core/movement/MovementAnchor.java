package net.zic.ascension.api.core.movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record MovementAnchor(
        Identifier dimension,
        Vec3 position,
        float yaw,
        float pitch,
        long expiresAt
) {
    public static final Codec<MovementAnchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(MovementAnchor::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(value -> value.position().x),
            Codec.DOUBLE.fieldOf("y").forGetter(value -> value.position().y),
            Codec.DOUBLE.fieldOf("z").forGetter(value -> value.position().z),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(MovementAnchor::yaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(MovementAnchor::pitch),
            Codec.LONG.optionalFieldOf("expires_at", 0L).forGetter(MovementAnchor::expiresAt)
    ).apply(instance, (dimension, x, y, z, yaw, pitch, expiresAt) ->
            new MovementAnchor(dimension, new Vec3(x, y, z), yaw, pitch, expiresAt)
    ));

    public boolean expired(long gameTime) {
        return expiresAt > 0L && gameTime >= expiresAt;
    }
}
