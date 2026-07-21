package net.zic.ascension.api.ascension.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;

public interface ProgressActionCondition {
    /**
     *
     * @param source the origin source this condition is called on
     * @param contextIdentifier the Identifier of the registry object this action is being queried for(e.g a path, technique, bloodline, physique etc)
     * @param  contextData the data for the contextIdentifier RegistryObject (e.g technique data or bloodline data)
     * @param direction is the expected progression gained/up or lost/down
     * @return true -> run all actions, false -> do not run actins
     */
    boolean test(OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction);


    ProgressActionConditionType getType();
}
