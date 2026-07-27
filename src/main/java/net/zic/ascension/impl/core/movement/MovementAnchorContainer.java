package net.zic.ascension.impl.core.movement;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.movement.MovementAnchor;

import java.util.HashMap;
import java.util.Map;

public final class MovementAnchorContainer {
    public static final Codec<MovementAnchorContainer> CODEC =
            Codec.unboundedMap(Identifier.CODEC, MovementAnchor.CODEC).xmap(
                    MovementAnchorContainer::new,
                    MovementAnchorContainer::anchors
            );

    private final Map<Identifier, MovementAnchor> anchors;

    public MovementAnchorContainer() {
        this.anchors = new HashMap<>();
    }

    private MovementAnchorContainer(Map<Identifier, MovementAnchor> anchors) {
        this.anchors = new HashMap<>(anchors == null ? Map.of() : anchors);
    }

    public Map<Identifier, MovementAnchor> anchors() {
        return Map.copyOf(anchors);
    }

    public MovementAnchor get(Identifier id, long gameTime) {
        MovementAnchor anchor = anchors.get(id);
        if (anchor != null && anchor.expired(gameTime)) {
            anchors.remove(id);
            return null;
        }
        return anchor;
    }

    public void put(Identifier id, MovementAnchor anchor) {
        anchors.put(id, anchor);
    }

    public boolean remove(Identifier id) {
        return anchors.remove(id) != null;
    }

    public void clear() {
        anchors.clear();
    }
}
