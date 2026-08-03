package net.zic.ascension.api.ascension.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public interface ProgressActionCondition {
    /**
     *
     * @param source the origin source this condition is called on
     * @param contextIdentifier the Identifier of the registry object this action is being queried for(e.g a path, technique, bloodline, physique etc)
     * @param  contextData the data for the contextIdentifier Object (e.g technique data or bloodline data)
     * @param direction is the expected progression gained/up or lost/down
     * @return true -> run all actions, false -> do not run actins
     */
    boolean test(OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction);


    ProgressActionConditionType getType();
}
