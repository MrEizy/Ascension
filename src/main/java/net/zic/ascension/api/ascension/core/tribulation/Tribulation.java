package net.zic.ascension.api.ascension.core.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

/**
 * A tribulation as defined in the tribulation registry.
 * Holds the configuration (strikes, damage, delay etc.) and the logic for running it.
 * Runtime state belongs in {@link TribulationData}
 */
public interface Tribulation {
    TribulationType getType();

    //creates fresh data, used both when a tribulation starts and when completed data needs to be created without running it
    TribulationData newData();

    //called once when the tribulation is first started (not when loaded from save)
    default void onStart(TribulationInstance instance, ServerLevel level) {}

    /**
     * runs every server tick while the chunk the tribulation is in is loaded.
     * call {@link TribulationInstance#succeed()} or {@link TribulationInstance#fail()} to finish it
     */
    void tick(TribulationInstance instance, ServerLevel level);

    //called when the tribulation finishes, before resolution actions are run
    default void onFinished(TribulationInstance instance, TribulationOutcome outcome) {}

    //called when a tracked target dies, by default this fails the tribulation
    default void onTargetDeath(TribulationInstance instance) {
        instance.fail();
    }

    //──Completed Data────────────────────────────────────────────────────────
    //completed tribulation data can be held by other systems(like paths) to apply bonuses

    default void onAdded(OriginSource source, TribulationData data) {}

    default void onRemoved(OriginSource source, TribulationData data) {}

    /**
     * called on loaded data (both active and completed) to ensure it still matches this tribulation,
     * the configuration may have changed since it was saved.
     * @return the data to use, or null to discard it
     */
    default @Nullable TribulationData validateLoadedData(TribulationData data) {
        return data;
    }
}
