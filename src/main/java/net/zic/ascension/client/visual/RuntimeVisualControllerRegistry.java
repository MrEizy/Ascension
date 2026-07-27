package net.zic.ascension.client.visual;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.client.visual.RuntimeVisualController;

import java.util.HashMap;
import java.util.Map;

public final class RuntimeVisualControllerRegistry {
    private static final Map<Identifier, RuntimeVisualController> CONTROLLERS = new HashMap<>();

    private RuntimeVisualControllerRegistry() {
    }

    public static void register(Identifier id, RuntimeVisualController controller) {
        if (id != null && controller != null) {
            CONTROLLERS.put(id, controller);
        }
    }

    public static RuntimeVisualController get(Identifier id) {
        return id == null ? null : CONTROLLERS.get(id);
    }

    public static void clear() {
        CONTROLLERS.clear();
    }
}
