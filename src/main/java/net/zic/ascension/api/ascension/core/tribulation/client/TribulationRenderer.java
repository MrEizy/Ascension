package net.zic.ascension.api.ascension.core.tribulation.client;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;

/**
 * client side hooks for a Tribulation, provided by {@link Tribulation#getRenderer()}.
 * must stay an interface so servers never load renderer classes
 */
public interface TribulationRenderer<T extends TribulationData> {
    TribulationRenderer<?> NONE = new TribulationRenderer<>() {};

    //a renderer that does nothing, used by tribulations without any visuals
    @SuppressWarnings("unchecked")
    static <T extends TribulationData> TribulationRenderer<T> none() {
        return (TribulationRenderer<T>) NONE;
    }

    //called every client tick for each tracked tribulation in the players dimension
    default void clientTick(ClientTribulation<T> tribulation) {}

    //called after translucent features are rendered
    default void render(ClientTribulation<T> tribulation, RenderLevelStageEvent event) {}

    //called when the tribulation is no longer tracked
    default void onRemoved(ClientTribulation<T> tribulation) {}
}
