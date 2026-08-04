package net.zic.ascension.client.visual.runtime;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ClientRuntimeVisuals {
    private static final Map<UUID, RuntimeVisualState> STATES = new HashMap<>();
    private static final Map<Identifier, RuntimeVisualController> CONTROLLERS = new HashMap<>();
    private static final RuntimeVisualController DATAPACK_CONTROLLER = new DatapackRuntimeVisualController();

    private ClientRuntimeVisuals() {
    }

    public static void register(Identifier id, RuntimeVisualController controller) {
        if (id != null && controller != null) {
            CONTROLLERS.put(id, controller);
        }
    }

    public static void accept(RuntimeVisualState.Action action, RuntimeVisualState state) {
        if (action == RuntimeVisualState.Action.REMOVE) {
            RuntimeVisualState removed = STATES.remove(state.runtimeId());
            RuntimeVisualController controller = removed == null ? null : controller(removed);
            if (controller != null) {
                controller.onRemove(removed);
            }
            return;
        }

        RuntimeVisualState previous = STATES.put(state.runtimeId(), state);
        if (previous != null && !java.util.Objects.equals(previous.visual(), state.visual())) {
            RuntimeVisualController previousController = controller(previous);
            if (previousController != null) {
                previousController.onRemove(previous);
            }
            previous = null;
        }

        RuntimeVisualController controller = controller(state);
        if (controller == null) {
            return;
        }
        if (previous == null || action == RuntimeVisualState.Action.SPAWN) {
            controller.onSpawn(state);
        } else {
            controller.onUpdate(previous, state);
        }
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clearStates();
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        Iterator<RuntimeVisualState> iterator = STATES.values().iterator();
        while (iterator.hasNext()) {
            RuntimeVisualState state = iterator.next();
            RuntimeVisualController controller = controller(state);
            if (state.expiresAt() > 0L && gameTime >= state.expiresAt()) {
                if (controller != null) {
                    controller.onRemove(state);
                }
                iterator.remove();
            } else if (controller != null) {
                controller.tick(state);
            }
        }
    }

    public static void render(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        for (RuntimeVisualState state : STATES.values()) {
            RuntimeVisualController controller = controller(state);
            if (controller != null) {
                controller.render(state, event);
            }
        }
    }

    public static RuntimeVisualState get(UUID runtimeId) {
        return STATES.get(runtimeId);
    }

    public static Vec3 position(RuntimeVisualState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!state.hasFlag(RuntimeVisualState.OWNER_RELATIVE)
                || state.ownerId() == null
                || minecraft.level == null) {
            return state.position();
        }

        Player owner = minecraft.level.getPlayerByUUID(state.ownerId());
        if (owner == null) {
            return state.position();
        }

        Vec3 offset = state.offset();
        if (state.hasFlag(RuntimeVisualState.ROTATE_WITH_OWNER)) {
            double radians = Math.toRadians(-owner.getYRot());
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            offset = new Vec3(
                    offset.x * cos - offset.z * sin,
                    offset.y,
                    offset.x * sin + offset.z * cos
            );
        }
        return owner.position().add(offset);
    }

    public static void clear() {
        clearStates();
        CONTROLLERS.clear();
    }

    static RuntimeVisualController registeredController(Identifier id) {
        return id == null ? null : CONTROLLERS.get(id);
    }

    private static RuntimeVisualController controller(RuntimeVisualState state) {
        if (state.visual() == null) {
            return null;
        }
        RuntimeVisualController controller = CONTROLLERS.get(state.visual());
        return controller == null ? DATAPACK_CONTROLLER : controller;
    }

    private static void clearStates() {
        for (RuntimeVisualState state : STATES.values()) {
            RuntimeVisualController controller = controller(state);
            if (controller != null) {
                controller.onRemove(state);
            }
        }
        STATES.clear();
    }
}
