package net.zic.ascension.api.ascension.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.UUID;

public interface ProgressAction {
    //an extra layer of salting to prevent duplicate keys among same type instances
    UUID getUniqueId();
    /**
     * performs a generic action on a source assuming the Condition was met
     *
     * Note, make sure to only call context specific actions WITHING THEIR SPECIFIC CONTEXT
     * as there may be a scenario where a physique/bloodline/path etc share an id, which can cause issues
     *
     * @param  holderId the unique id of the holder Holing this, can be used to avoid duplicate keys
     * @param source the origin source this condition is called on
     * @param contextIdentifier the Identifier of the registry object this action is being queried for(e.g a path, technique, bloodline, physique etc)
     * @param  contextData the data for the contextIdentifier RegistryObject (e.g technique data or bloodline data)
     * @param direction is the expected progression gained/up or lost/down
     */
    void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction);

    ProgressActionType getType();
}
