package net.zic.ascension.api.ascension.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import org.jspecify.annotations.Nullable;

/**
 * A tribulation as defined in the tribulation registry.
 * Holds the configuration (strikes, damage, delay etc.) and the logic for running it.
 * Runtime state belongs in {@link TribulationData}
 * @param <T> the data this tribulation creates and runs on
 */
public interface Tribulation<T extends TribulationData> {
    TribulationType<T> getType();

    //creates fresh data, used both when a tribulation starts and when completed data needs to be created without running it
    T newData();

    //called once when the tribulation is first started (not when loaded from save)
    default void onStart(TribulationInstance<T> instance, ServerLevel level) {}

    /**
     * runs every server tick while the chunk the tribulation is in is loaded.
     * call {@link TribulationInstance#succeed()} or {@link TribulationInstance#fail()} to finish it
     */
    void tick(TribulationInstance<T> instance, ServerLevel level);

    //called when the tribulation finishes, before resolution actions are run
    default void onFinished(TribulationInstance<T> instance, TribulationOutcome outcome) {}

    //called when a tracked target dies, by default this fails the tribulation
    default void onTargetDeath(TribulationInstance<T> instance) {
        instance.fail();
    }

    //──Completed Data────────────────────────────────────────────────────────
    //completed tribulation data can be held by other systems(like paths) to apply bonuses

    default void onAdded(OriginSource source, T data) {}

    default void onRemoved(OriginSource source, T data) {}

    /**
     * called on loaded data (both active and completed) to ensure it still matches this tribulation,
     * the configuration may have changed since it was saved.
     * @return the data to use, or null to discard it
     */
    default @Nullable T validateLoadedData(T data) {
        return data;
    }

    //──Client────────────────────────────────────────────────────────

    /**
     * the renderer for this tribulation. <b>Only call this from client code.</b>
     * <p>
     * Create the renderer lazily and cache it in a field, passing {@code this} to its constructor
     * so the renderer has access to the concrete tribulation and its configuration.
     * <p>
     * This is safe on dedicated servers since {@link TribulationRenderer} is an interface, the renderer class is never
     * loaded unless this method runs. Keep it an interface.
     */
    default TribulationRenderer<T> getRenderer() {
        return TribulationRenderer.none();
    }
}
