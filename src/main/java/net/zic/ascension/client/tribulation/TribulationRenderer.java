package net.zic.ascension.client.tribulation;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * client side hooks for a TribulationType, register with {@link TribulationRenderers#register}
 */
public interface TribulationRenderer {

    //called every client tick for each tracked tribulation of this type in the players dimension
    default void clientTick(ClientTribulation tribulation) {}

    //called after translucent features are rendered
    default void render(ClientTribulation tribulation, RenderLevelStageEvent event) {}

    //called when the tribulation is no longer tracked
    default void onRemoved(ClientTribulation tribulation) {}
}
