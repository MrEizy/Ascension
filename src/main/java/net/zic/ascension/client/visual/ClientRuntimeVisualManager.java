package net.zic.ascension.client.visual;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.client.visual.RuntimeVisualAction;
import net.zic.ascension.api.client.visual.RuntimeVisualController;
import net.zic.ascension.api.client.visual.RuntimeVisualState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ClientRuntimeVisualManager {
    private static final Map<UUID, RuntimeVisualState> STATES = new HashMap<>();

    private ClientRuntimeVisualManager() {
    }

    public static void accept(RuntimeVisualAction action, RuntimeVisualState state) {
        if (action == RuntimeVisualAction.REMOVE) {
            RuntimeVisualState removed = STATES.remove(state.runtimeId());
            if (removed != null) {
                RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(removed.visual());
                if (controller != null) {
                    controller.onRemove(removed);
                }
            }
            return;
        }

        RuntimeVisualState previous = STATES.put(state.runtimeId(), state);
        if (previous != null
                && previous.visual() != null
                && !previous.visual().equals(state.visual())) {
            RuntimeVisualController previousController =
                    RuntimeVisualControllerRegistry.get(previous.visual());
            if (previousController != null) {
                previousController.onRemove(previous);
            }
            previous = null;
        }

        RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(state.visual());
        if (controller == null) {
            return;
        }
        if (previous == null || action == RuntimeVisualAction.SPAWN) {
            controller.onSpawn(state);
        } else {
            controller.onUpdate(previous, state);
        }
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        Iterator<RuntimeVisualState> iterator = STATES.values().iterator();
        while (iterator.hasNext()) {
            RuntimeVisualState state = iterator.next();
            if (state.expiresAt() > 0L && gameTime >= state.expiresAt()) {
                RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(state.visual());
                if (controller != null) {
                    controller.onRemove(state);
                }
                iterator.remove();
                continue;
            }
            RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(state.visual());
            if (controller != null) {
                controller.tick(state);
            }
        }
    }

    public static void render(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        for (RuntimeVisualState state : STATES.values()) {
            RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(state.visual());
            if (controller != null) {
                controller.render(state, event);
            }
        }
    }

    public static RuntimeVisualState get(UUID runtimeId) {
        return STATES.get(runtimeId);
    }

    public static void clear() {
        for (RuntimeVisualState state : STATES.values()) {
            RuntimeVisualController controller = RuntimeVisualControllerRegistry.get(state.visual());
            if (controller != null) {
                controller.onRemove(state);
            }
        }
        STATES.clear();
    }
}
