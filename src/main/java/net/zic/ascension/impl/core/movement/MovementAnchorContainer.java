package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class MovementAnchorContainer {
    public static final Codec<MovementAnchorContainer> CODEC = Codec.unboundedMap(Identifier.CODEC, Anchor.CODEC).xmap(
            MovementAnchorContainer::new,
            MovementAnchorContainer::anchors
    );

    private final Map<Identifier, Anchor> anchors;

    public MovementAnchorContainer() {
        this.anchors = new HashMap<>();
    }

    private MovementAnchorContainer(Map<Identifier, Anchor> anchors) {
        this.anchors = new HashMap<>(anchors == null ? Map.of() : anchors);
    }

    public Map<Identifier, Anchor> anchors() {
        return Map.copyOf(anchors);
    }

    public Anchor get(Identifier id, long gameTime) {
        Anchor anchor = anchors.get(id);
        if (anchor != null && anchor.expired(gameTime)) {
            anchors.remove(id);
            return null;
        }
        return anchor;
    }

    public void put(Identifier id, Anchor anchor) {
        anchors.put(id, anchor);
    }

    public boolean remove(Identifier id) {
        return anchors.remove(id) != null;
    }

    public void clear() {
        anchors.clear();
    }

    public record Anchor(Identifier dimension, Vec3 position, float yaw, float pitch, long expiresAt) {
        public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("dimension").forGetter(Anchor::dimension),
                Codec.DOUBLE.fieldOf("x").forGetter(value -> value.position().x),
                Codec.DOUBLE.fieldOf("y").forGetter(value -> value.position().y),
                Codec.DOUBLE.fieldOf("z").forGetter(value -> value.position().z),
                Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(Anchor::yaw),
                Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(Anchor::pitch),
                Codec.LONG.optionalFieldOf("expires_at", 0L).forGetter(Anchor::expiresAt)
        ).apply(instance, (dimension, x, y, z, yaw, pitch, expiresAt) ->
                new Anchor(dimension, new Vec3(x, y, z), yaw, pitch, expiresAt)
        ));

        public boolean expired(long gameTime) {
            return expiresAt > 0L && gameTime >= expiresAt;
        }
    }
}
