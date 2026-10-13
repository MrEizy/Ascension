package net.zic.ascension.api.ascension.core.tribulation;

import net.zic.ascension.api.ascension.datapack.tribulation.TribulationResolutionActionType;

/**
 * Runs when a tribulation finishes, saved alongside the active tribulation.
 * Actions should verify any state they rely on, since the world/datapack may have changed
 * between the tribulation starting and finishing.
 */
public interface TribulationResolutionAction {
    TribulationResolutionActionType getType();

    void resolve(TribulationResolutionContext context);
}
