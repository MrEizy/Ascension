package net.zic.ascension.api.client.visual;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public interface RuntimeVisualController {
    default void onSpawn(RuntimeVisualState state) {
    }

    default void onUpdate(RuntimeVisualState previous, RuntimeVisualState current) {
    }

    default void onRemove(RuntimeVisualState state) {
    }

    default void tick(RuntimeVisualState state) {
    }

    default void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
    }
}
